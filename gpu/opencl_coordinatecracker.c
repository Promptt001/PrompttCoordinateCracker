/*
 * Optional OpenCL helper for Promptt Coordinate Cracker.
 *
 * Build this separately and launch the Java app with:
 *   java -Dcoordinatecracker.gpuCommand=/path/to/coordinatecracker-opencl-helper -jar Promptts_Coordinate_Cracker.jar
 *
 * The Java jar does not link OpenCL directly. It writes one or more scan requests to this helper's
 * stdin; the helper keeps one OpenCL context/program/kernels alive and prints MATCH/DONE lines for each request.
 *
 * PCCGPU4 uses a packed plane-sieve: random states are generated once into row-padded bit planes,
 * then candidate words are filtered by shifted mask intersections.
 */
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

#define MAX_PATTERNS 12
#define MAX_OBSERVATIONS 4096
#define MAX_ERROR_TEXT 4096
#define MASKS_PER_PLANE 6

struct Observation {
    cl_int dx;
    cl_int dy;
    cl_int dz;
    cl_int wanted;
    cl_int visibleMapping;
};

struct PatternDef {
    cl_int obsOffset;
    cl_int obsCount;
    cl_int facing;
};

struct Match {
    cl_int x;
    cl_int y;
    cl_int z;
    cl_int facing;
};

struct ScanRequest {
    int version;
    int minX;
    int maxXExclusive;
    int minZ;
    int maxZExclusive;
    int yStart;
    int yEnd;
    int maxMatches;
    int patternCount;
    int observationCount;
    struct PatternDef patterns[MAX_PATTERNS];
    struct Observation observations[MAX_OBSERVATIONS];
};

struct ScanExtents {
    int minDx;
    int maxDx;
    int minDy;
    int maxDy;
    int minDz;
    int maxDz;
    int width;
    int depth;
    int yRange;
    int extWidth;
    int extDepth;
    int extWordsPerRow;
    int candidateWordsPerRow;
    int lookupYStart;
    int lookupYCount;
};

struct GpuContext {
    cl_context context;
    cl_command_queue queue;
    cl_program program;
    cl_kernel buildMasksKernel;
    cl_kernel sieveKernel;
    cl_mem patternBuffer;
    cl_mem observationBuffer;
    cl_mem maskBuffer;
    cl_mem matchBuffer;
    cl_mem countBuffer;
    cl_mem overflowBuffer;
    size_t patternCapacityBytes;
    size_t observationCapacityBytes;
    size_t maskCapacityBytes;
    size_t matchCapacityBytes;
};

static const char *KERNEL_SOURCE =
"typedef struct { int dx; int dy; int dz; int wanted; int visibleMapping; } Observation;\n"
"typedef struct { int obsOffset; int obsCount; int facing; } PatternDef;\n"
"typedef struct { int x; int y; int z; int facing; } Match;\n"
"ulong low_bits_mask(int bits) {\n"
"    return bits >= 64 ? ~0UL : ((1UL << bits) - 1UL);\n"
"}\n"
"int variant4_1_21_11(int x, int y, int z) {\n"
"    int xprod = (int)((uint)x * (uint)3129871);\n"
"    long i = (long)xprod ^ ((long)z * 116129781L) ^ (long)y;\n"
"    ulong u = as_ulong(i);\n"
"    u = u * u * 42317861UL + u * 11UL;\n"
"    long mixed = as_long(u);\n"
"    long seed = mixed >> 16;\n"
"    ulong randomSeed = ((ulong)(seed ^ 25214903917L)) & 281474976710655UL;\n"
"    randomSeed = (randomSeed * 25214903917UL + 11UL) & 281474976710655UL;\n"
"    return (int)(randomSeed >> 46);\n"
"}\n"
"__kernel void build_masks(\n"
"    int extMinX, int extMinZ, int lookupYStart, int extWidth, int extDepth, int extWordsPerRow,\n"
"    __global ulong *stateMasks) {\n"
"    int wordInRow = (int)get_global_id(0);\n"
"    int localZ = (int)get_global_id(1);\n"
"    int lookupYIndex = (int)get_global_id(2);\n"
"    if(wordInRow >= extWordsPerRow || localZ >= extDepth) return;\n"
"    int xBase = wordInRow << 6;\n"
"    int bitsInWord = extWidth - xBase;\n"
"    if(bitsInWord > 64) bitsInWord = 64;\n"
"    if(bitsInWord < 0) bitsInWord = 0;\n"
"    ulong m0 = 0UL;\n"
"    ulong m1 = 0UL;\n"
"    ulong m2 = 0UL;\n"
"    ulong m3 = 0UL;\n"
"    ulong p0 = 0UL;\n"
"    ulong p1 = 0UL;\n"
"    int y = lookupYStart + lookupYIndex;\n"
"    int z = extMinZ + localZ;\n"
"    for(int bit = 0; bit < bitsInWord; ++bit) {\n"
"        int x = extMinX + xBase + bit;\n"
"        int state = variant4_1_21_11(x, y, z) & 3;\n"
"        ulong bitMask = 1UL << bit;\n"
"        if(state == 0) { m0 |= bitMask; p0 |= bitMask; }\n"
"        else if(state == 1) { m1 |= bitMask; p1 |= bitMask; }\n"
"        else if(state == 2) { m2 |= bitMask; p0 |= bitMask; }\n"
"        else { m3 |= bitMask; p1 |= bitMask; }\n"
"    }\n"
"    size_t planeWords = (size_t)extDepth * (size_t)extWordsPerRow;\n"
"    size_t base = ((size_t)lookupYIndex * 6UL * planeWords) + ((size_t)localZ * (size_t)extWordsPerRow) + (size_t)wordInRow;\n"
"    stateMasks[base + 0UL * planeWords] = m0;\n"
"    stateMasks[base + 1UL * planeWords] = m1;\n"
"    stateMasks[base + 2UL * planeWords] = m2;\n"
"    stateMasks[base + 3UL * planeWords] = m3;\n"
"    stateMasks[base + 4UL * planeWords] = p0;\n"
"    stateMasks[base + 5UL * planeWords] = p1;\n"
"}\n"
"__kernel void sieve_rect(\n"
"    int minX, int minZ, int yStart, int width, int depth, int yRange,\n"
"    int minDx, int minDy, int minDz, int extDepth, int extWordsPerRow, int candidateWordsPerRow,\n"
"    int patternCount, __global const PatternDef *patterns, __global const Observation *observations,\n"
"    __global const ulong *stateMasks, __global Match *matches, volatile __global unsigned int *matchCount,\n"
"    volatile __global unsigned int *overflow, int maxMatches) {\n"
"    int wordInRow = (int)get_global_id(0);\n"
"    int localZ = (int)get_global_id(1);\n"
"    int localY = (int)get_global_id(2);\n"
"    if(wordInRow >= candidateWordsPerRow || localZ >= depth || localY >= yRange) return;\n"
"    int xBase = wordInRow << 6;\n"
"    int bitsInWord = width - xBase;\n"
"    if(bitsInWord > 64) bitsInWord = 64;\n"
"    if(bitsInWord <= 0) return;\n"
"    ulong activeMask = low_bits_mask(bitsInWord);\n"
"    int y = yStart + localY;\n"
"    size_t planeWords = (size_t)extDepth * (size_t)extWordsPerRow;\n"
"    for(int p = 0; p < patternCount; ++p) {\n"
"        PatternDef pattern = patterns[p];\n"
"        ulong candidateBits = activeMask;\n"
"        for(int o = 0; o < pattern.obsCount; ++o) {\n"
"            Observation obs = observations[pattern.obsOffset + o];\n"
"            if(obs.visibleMapping == 2) {\n"
"                if(obs.wanted != 0) candidateBits = 0UL;\n"
"                if(candidateBits == 0UL) break;\n"
"                continue;\n"
"            }\n"
"            int maskIndex = obs.visibleMapping == 1 ? 4 + obs.wanted : obs.wanted;\n"
"            int lookupYIndex = localY + obs.dy - minDy;\n"
"            int sourceZ = localZ + obs.dz - minDz;\n"
"            int sourceBitIndex = xBase + obs.dx - minDx;\n"
"            int sourceWord = sourceBitIndex >> 6;\n"
"            int sourceBit = sourceBitIndex & 63;\n"
"            size_t rowIndex = (((size_t)lookupYIndex * 6UL + (size_t)maskIndex) * planeWords) + ((size_t)sourceZ * (size_t)extWordsPerRow) + (size_t)sourceWord;\n"
"            ulong allowed = stateMasks[rowIndex] >> sourceBit;\n"
"            if(sourceBit != 0 && sourceWord + 1 < extWordsPerRow) {\n"
"                allowed |= stateMasks[rowIndex + 1] << (64 - sourceBit);\n"
"            }\n"
"            candidateBits &= allowed;\n"
"            if(candidateBits == 0UL) break;\n"
"        }\n"
"        if(candidateBits != 0UL) {\n"
"            for(int bit = 0; bit < bitsInWord; ++bit) {\n"
"                if((candidateBits & (1UL << bit)) != 0UL) {\n"
"                    unsigned int slot = atomic_inc(matchCount);\n"
"                    if(slot < (unsigned int)maxMatches) {\n"
"                        matches[slot].x = minX + xBase + bit;\n"
"                        matches[slot].y = y;\n"
"                        matches[slot].z = minZ + localZ;\n"
"                        matches[slot].facing = pattern.facing;\n"
"                    } else {\n"
"                        atomic_inc(overflow);\n"
"                    }\n"
"                }\n"
"            }\n"
"        }\n"
"    }\n"
"}\n";

static void die(const char *message) {
    fprintf(stdout, "ERROR %s\n", message);
    fflush(stdout);
}

static int read_word(char *buffer, size_t size) {
    (void)size;
    return scanf("%63s", buffer) == 1;
}

static void reset_request(struct ScanRequest *request) {
    memset(request, 0, sizeof(*request));
    request->maxMatches = 1048576;
}

static int choose_device(cl_platform_id *platform_out, cl_device_id *device_out) {
    cl_uint platform_count = 0;
    if(clGetPlatformIDs(0, NULL, &platform_count) != CL_SUCCESS || platform_count == 0) {
        return 0;
    }

    cl_platform_id *platforms = (cl_platform_id *)calloc(platform_count, sizeof(cl_platform_id));
    if(!platforms) return 0;
    if(clGetPlatformIDs(platform_count, platforms, NULL) != CL_SUCCESS) {
        free(platforms);
        return 0;
    }

    for(cl_uint p = 0; p < platform_count; ++p) {
        cl_uint device_count = 0;
        if(clGetDeviceIDs(platforms[p], CL_DEVICE_TYPE_GPU, 0, NULL, &device_count) == CL_SUCCESS && device_count > 0) {
            cl_device_id *devices = (cl_device_id *)calloc(device_count, sizeof(cl_device_id));
            if(!devices) continue;
            if(clGetDeviceIDs(platforms[p], CL_DEVICE_TYPE_GPU, device_count, devices, NULL) == CL_SUCCESS) {
                *platform_out = platforms[p];
                *device_out = devices[0];
                free(devices);
                free(platforms);
                return 1;
            }
            free(devices);
        }
    }

    free(platforms);
    return 0;
}

static int probe(void) {
    cl_platform_id platform;
    cl_device_id device;
    char name[256] = {0};
    if(!choose_device(&platform, &device)) {
        printf("ERROR No OpenCL GPU device found.\n");
        return 1;
    }
    (void)platform;
    clGetDeviceInfo(device, CL_DEVICE_NAME, sizeof(name), name, NULL);
    printf("OpenCL GPU device: %s; protocol PCCGPU4 packed plane sieve\n", name[0] ? name : "unknown");
    return 0;
}

static int parse_request(struct ScanRequest *request) {
    char word[64];
    reset_request(request);

    if(!read_word(word, sizeof(word))) {
        return 0;
    }
    if(strcmp(word, "PCCGPU4") != 0) {
        die("Expected PCCGPU4 request header.");
        return -1;
    }

    int sawEnd = 0;
    while(read_word(word, sizeof(word))) {
        if(strcmp(word, "VERSION") == 0) {
            if(scanf("%d", &request->version) != 1) { die("Bad VERSION line."); return -1; }
        } else if(strcmp(word, "RECT") == 0) {
            if(scanf("%d %d %d %d %d %d", &request->minX, &request->maxXExclusive, &request->minZ, &request->maxZExclusive, &request->yStart, &request->yEnd) != 6) { die("Bad RECT line."); return -1; }
        } else if(strcmp(word, "MAX_MATCHES") == 0) {
            if(scanf("%d", &request->maxMatches) != 1 || request->maxMatches < 1) { die("Bad MAX_MATCHES line."); return -1; }
        } else if(strcmp(word, "PATTERNS") == 0) {
            if(scanf("%d", &request->patternCount) != 1 || request->patternCount < 1 || request->patternCount > MAX_PATTERNS) { die("Bad PATTERNS line."); return -1; }
            for(int p = 0; p < request->patternCount; ++p) {
                int facing = 0;
                int obsCount = 0;
                if(!read_word(word, sizeof(word)) || strcmp(word, "PATTERN") != 0 || scanf("%d %d", &facing, &obsCount) != 2) { die("Bad PATTERN line."); return -1; }
                if(facing < 0 || facing > 11) { die("Bad pattern facing."); return -1; }
                if(obsCount < 1 || request->observationCount + obsCount > MAX_OBSERVATIONS) { die("Too many observations for helper limits."); return -1; }
                request->patterns[p].obsOffset = request->observationCount;
                request->patterns[p].obsCount = obsCount;
                request->patterns[p].facing = facing;
                for(int o = 0; o < obsCount; ++o) {
                    struct Observation *obs = &request->observations[request->observationCount++];
                    if(scanf("%d %d %d %d %d", &obs->dx, &obs->dy, &obs->dz, &obs->wanted, &obs->visibleMapping) != 5) {
                        die("Bad observation line.");
                        return -1;
                    }
                }
            }
        } else if(strcmp(word, "END") == 0) {
            sawEnd = 1;
            break;
        } else {
            die("Unknown request token.");
            return -1;
        }
    }

    if(!sawEnd) { die("Unexpected end of input before END."); return -1; }
    if(request->version != 12111) { die("Only VERSION 12111 is supported by this helper."); return -1; }
    if(request->maxXExclusive <= request->minX || request->maxZExclusive <= request->minZ || request->yEnd <= request->yStart) { die("Empty scan rectangle."); return -1; }
    if(request->patternCount < 1 || request->observationCount < 1) { die("No patterns supplied."); return -1; }
    return 1;
}

static int init_gpu_context(struct GpuContext *gpu) {
    memset(gpu, 0, sizeof(*gpu));
    cl_int err = CL_SUCCESS;
    cl_platform_id platform;
    cl_device_id device;
    if(!choose_device(&platform, &device)) { die("No OpenCL GPU device found."); return 0; }

    gpu->context = clCreateContext(NULL, 1, &device, NULL, NULL, &err);
    if(err != CL_SUCCESS) { die("Failed to create OpenCL context."); return 0; }

#if CL_TARGET_OPENCL_VERSION >= 200
    gpu->queue = clCreateCommandQueueWithProperties(gpu->context, device, NULL, &err);
#else
    gpu->queue = clCreateCommandQueue(gpu->context, device, 0, &err);
#endif
    if(err != CL_SUCCESS) { die("Failed to create OpenCL command queue."); return 0; }

    size_t source_len = strlen(KERNEL_SOURCE);
    gpu->program = clCreateProgramWithSource(gpu->context, 1, &KERNEL_SOURCE, &source_len, &err);
    if(err != CL_SUCCESS) { die("Failed to create OpenCL program."); return 0; }

    err = clBuildProgram(gpu->program, 1, &device, "", NULL, NULL);
    if(err != CL_SUCCESS) {
        char log[MAX_ERROR_TEXT];
        size_t log_size = 0;
        clGetProgramBuildInfo(gpu->program, device, CL_PROGRAM_BUILD_LOG, sizeof(log) - 1, log, &log_size);
        log[log_size < sizeof(log) ? log_size : sizeof(log) - 1] = '\0';
        fprintf(stdout, "ERROR OpenCL build failed: %s\n", log);
        fflush(stdout);
        return 0;
    }

    gpu->buildMasksKernel = clCreateKernel(gpu->program, "build_masks", &err);
    if(err != CL_SUCCESS) { die("Failed to create OpenCL build_masks kernel."); return 0; }
    gpu->sieveKernel = clCreateKernel(gpu->program, "sieve_rect", &err);
    if(err != CL_SUCCESS) { die("Failed to create OpenCL sieve_rect kernel."); return 0; }
    return 1;
}

static void release_gpu_context(struct GpuContext *gpu) {
    if(gpu->overflowBuffer) clReleaseMemObject(gpu->overflowBuffer);
    if(gpu->countBuffer) clReleaseMemObject(gpu->countBuffer);
    if(gpu->matchBuffer) clReleaseMemObject(gpu->matchBuffer);
    if(gpu->maskBuffer) clReleaseMemObject(gpu->maskBuffer);
    if(gpu->observationBuffer) clReleaseMemObject(gpu->observationBuffer);
    if(gpu->patternBuffer) clReleaseMemObject(gpu->patternBuffer);
    if(gpu->sieveKernel) clReleaseKernel(gpu->sieveKernel);
    if(gpu->buildMasksKernel) clReleaseKernel(gpu->buildMasksKernel);
    if(gpu->program) clReleaseProgram(gpu->program);
    if(gpu->queue) clReleaseCommandQueue(gpu->queue);
    if(gpu->context) clReleaseContext(gpu->context);
    memset(gpu, 0, sizeof(*gpu));
}

static int ensure_buffer(struct GpuContext *gpu, cl_mem *buffer, size_t *capacityBytes, size_t neededBytes, cl_mem_flags flags, const char *name) {
    if(neededBytes == 0) neededBytes = 1;
    if(*buffer && *capacityBytes >= neededBytes) {
        return 1;
    }
    if(*buffer) {
        clReleaseMemObject(*buffer);
        *buffer = NULL;
        *capacityBytes = 0;
    }

    cl_int err = CL_SUCCESS;
    *buffer = clCreateBuffer(gpu->context, flags, neededBytes, NULL, &err);
    if(err != CL_SUCCESS) {
        char message[192];
        snprintf(message, sizeof(message), "Failed to allocate reusable %s buffer (%zu bytes).", name, neededBytes);
        die(message);
        return 0;
    }
    *capacityBytes = neededBytes;
    return 1;
}

static int compute_extents(const struct ScanRequest *request, struct ScanExtents *extents) {
    memset(extents, 0, sizeof(*extents));
    int initialized = 0;
    for(int i = 0; i < request->observationCount; ++i) {
        const struct Observation *obs = &request->observations[i];
        if(obs->visibleMapping == 2) {
            continue;
        }
        if(!initialized) {
            extents->minDx = extents->maxDx = obs->dx;
            extents->minDy = extents->maxDy = obs->dy;
            extents->minDz = extents->maxDz = obs->dz;
            initialized = 1;
            continue;
        }
        if(obs->dx < extents->minDx) extents->minDx = obs->dx;
        if(obs->dx > extents->maxDx) extents->maxDx = obs->dx;
        if(obs->dy < extents->minDy) extents->minDy = obs->dy;
        if(obs->dy > extents->maxDy) extents->maxDy = obs->dy;
        if(obs->dz < extents->minDz) extents->minDz = obs->dz;
        if(obs->dz > extents->maxDz) extents->maxDz = obs->dz;
    }
    if(!initialized) {
        die("No coordinate-constraining observations were supplied to the GPU sieve.");
        return 0;
    }

    extents->width = request->maxXExclusive - request->minX;
    extents->depth = request->maxZExclusive - request->minZ;
    extents->yRange = request->yEnd - request->yStart;
    extents->extWidth = extents->width + extents->maxDx - extents->minDx;
    extents->extDepth = extents->depth + extents->maxDz - extents->minDz;
    extents->extWordsPerRow = (extents->extWidth + 63) >> 6;
    extents->candidateWordsPerRow = (extents->width + 63) >> 6;
    extents->lookupYStart = request->yStart + extents->minDy;
    extents->lookupYCount = extents->yRange + extents->maxDy - extents->minDy;

    return extents->width > 0 && extents->depth > 0 && extents->yRange > 0
        && extents->extWidth > 0 && extents->extDepth > 0 && extents->lookupYCount > 0;
}

static int checked_mul_size(size_t a, size_t b, size_t *out) {
    if(a != 0 && b > ((size_t)-1) / a) return 0;
    *out = a * b;
    return 1;
}

static int run_request(struct GpuContext *gpu, const struct ScanRequest *request) {
    cl_int err = CL_SUCCESS;
    struct Match *matches = NULL;
    unsigned int zero = 0;
    unsigned int matchCount = 0;
    unsigned int overflow = 0;
    int ok = 0;
    struct ScanExtents extents;
    size_t patternBytes = 0;
    size_t observationBytes = 0;
    size_t maskWords = 0;
    size_t maskBytes = 0;
    size_t matchBytes = 0;

    if(!compute_extents(request, &extents)) { die("Bad computed scan extents."); goto cleanup; }

    patternBytes = sizeof(struct PatternDef) * (size_t)request->patternCount;
    observationBytes = sizeof(struct Observation) * (size_t)request->observationCount;
    if(!checked_mul_size(sizeof(struct Match), (size_t)request->maxMatches, &matchBytes)) { die("Match buffer size overflow."); goto cleanup; }

    size_t planeWords = 0;
    size_t maskPlanes = 0;
    if(!checked_mul_size((size_t)extents.extDepth, (size_t)extents.extWordsPerRow, &planeWords)
        || !checked_mul_size((size_t)extents.lookupYCount, (size_t)MASKS_PER_PLANE, &maskPlanes)
        || !checked_mul_size(maskPlanes, planeWords, &maskWords)
        || !checked_mul_size(maskWords, sizeof(cl_ulong), &maskBytes)) {
        die("State mask buffer size overflow.");
        goto cleanup;
    }

    if(!ensure_buffer(gpu, &gpu->patternBuffer, &gpu->patternCapacityBytes, patternBytes, CL_MEM_READ_ONLY, "pattern")) goto cleanup;
    if(!ensure_buffer(gpu, &gpu->observationBuffer, &gpu->observationCapacityBytes, observationBytes, CL_MEM_READ_ONLY, "observation")) goto cleanup;
    if(!ensure_buffer(gpu, &gpu->maskBuffer, &gpu->maskCapacityBytes, maskBytes, CL_MEM_READ_WRITE, "state-mask")) goto cleanup;
    if(!ensure_buffer(gpu, &gpu->matchBuffer, &gpu->matchCapacityBytes, matchBytes, CL_MEM_WRITE_ONLY, "match")) goto cleanup;
    if(!gpu->countBuffer) {
        size_t ignoredCapacity = 0;
        if(!ensure_buffer(gpu, &gpu->countBuffer, &ignoredCapacity, sizeof(unsigned int), CL_MEM_READ_WRITE, "count")) goto cleanup;
    }
    if(!gpu->overflowBuffer) {
        size_t ignoredCapacity = 0;
        if(!ensure_buffer(gpu, &gpu->overflowBuffer, &ignoredCapacity, sizeof(unsigned int), CL_MEM_READ_WRITE, "overflow")) goto cleanup;
    }

    err = clEnqueueWriteBuffer(gpu->queue, gpu->patternBuffer, CL_TRUE, 0, patternBytes, request->patterns, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to upload pattern buffer."); goto cleanup; }
    err = clEnqueueWriteBuffer(gpu->queue, gpu->observationBuffer, CL_TRUE, 0, observationBytes, request->observations, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to upload observation buffer."); goto cleanup; }
    err = clEnqueueWriteBuffer(gpu->queue, gpu->countBuffer, CL_TRUE, 0, sizeof(unsigned int), &zero, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to reset count buffer."); goto cleanup; }
    err = clEnqueueWriteBuffer(gpu->queue, gpu->overflowBuffer, CL_TRUE, 0, sizeof(unsigned int), &zero, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to reset overflow buffer."); goto cleanup; }

    int extMinX = request->minX + extents.minDx;
    int extMinZ = request->minZ + extents.minDz;
    int arg = 0;
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extMinX);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extMinZ);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extents.lookupYStart);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extents.extWidth);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extents.extDepth);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_int), &extents.extWordsPerRow);
    clSetKernelArg(gpu->buildMasksKernel, arg++, sizeof(cl_mem), &gpu->maskBuffer);

    size_t buildGlobal[3] = {
        (size_t)extents.extWordsPerRow,
        (size_t)extents.extDepth,
        (size_t)extents.lookupYCount
    };
    err = clEnqueueNDRangeKernel(gpu->queue, gpu->buildMasksKernel, 3, NULL, buildGlobal, NULL, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to enqueue OpenCL build_masks kernel."); goto cleanup; }

    arg = 0;
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &request->minX);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &request->minZ);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &request->yStart);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.width);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.depth);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.yRange);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.minDx);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.minDy);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.minDz);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.extDepth);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.extWordsPerRow);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &extents.candidateWordsPerRow);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &request->patternCount);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->patternBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->observationBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->maskBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->matchBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->countBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_mem), &gpu->overflowBuffer);
    clSetKernelArg(gpu->sieveKernel, arg++, sizeof(cl_int), &request->maxMatches);

    size_t sieveGlobal[3] = {
        (size_t)extents.candidateWordsPerRow,
        (size_t)extents.depth,
        (size_t)extents.yRange
    };
    err = clEnqueueNDRangeKernel(gpu->queue, gpu->sieveKernel, 3, NULL, sieveGlobal, NULL, 0, NULL, NULL);
    if(err != CL_SUCCESS) { die("Failed to enqueue OpenCL sieve_rect kernel."); goto cleanup; }
    clFinish(gpu->queue);

    clEnqueueReadBuffer(gpu->queue, gpu->countBuffer, CL_TRUE, 0, sizeof(unsigned int), &matchCount, 0, NULL, NULL);
    clEnqueueReadBuffer(gpu->queue, gpu->overflowBuffer, CL_TRUE, 0, sizeof(unsigned int), &overflow, 0, NULL, NULL);

    unsigned int readable = matchCount < (unsigned int)request->maxMatches ? matchCount : (unsigned int)request->maxMatches;
    if(readable > 0) {
        matches = (struct Match *)calloc(readable, sizeof(struct Match));
        if(!matches) { die("Failed to allocate host match buffer."); goto cleanup; }
        clEnqueueReadBuffer(gpu->queue, gpu->matchBuffer, CL_TRUE, 0, sizeof(struct Match) * (size_t)readable, matches, 0, NULL, NULL);
        for(unsigned int i = 0; i < readable; ++i) {
            printf("MATCH %d %d %d %d\n", matches[i].x, matches[i].y, matches[i].z, matches[i].facing);
        }
    }
    printf("DONE %u %u\n", readable, overflow);
    fflush(stdout);
    ok = 1;

cleanup:
    if(matches) free(matches);
    return ok;
}

int main(int argc, char **argv) {
    if(argc > 1 && strcmp(argv[1], "--probe") == 0) {
        return probe();
    }

    struct GpuContext gpu;
    struct ScanRequest request;
    if(!init_gpu_context(&gpu)) {
        release_gpu_context(&gpu);
        return 3;
    }

    for(;;) {
        int parsed = parse_request(&request);
        if(parsed == 0) {
            break;
        }
        if(parsed < 0) {
            release_gpu_context(&gpu);
            return 2;
        }
        if(!run_request(&gpu, &request)) {
            release_gpu_context(&gpu);
            return 3;
        }
    }

    release_gpu_context(&gpu);
    return 0;
}
