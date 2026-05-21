package io.github.promptt001.coordinatecracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.image.AffineTransformOp;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import io.github.promptt001.coordinatecracker.cracker.CoordinateBruteforcer;
import io.github.promptt001.coordinatecracker.cracker.ScanLaunchRequest;
import io.github.promptt001.coordinatecracker.data.EnumAccelerationMode;
import io.github.promptt001.coordinatecracker.data.EnumBlockType;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumMode;
import io.github.promptt001.coordinatecracker.data.EnumReason;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.gui.ErrorPopup;
import io.github.promptt001.coordinatecracker.gui.BlockTypeListCellRenderer;
import io.github.promptt001.coordinatecracker.gui.SquareGridPanel;
import io.github.promptt001.coordinatecracker.gui.TileIconRenderer;
import io.github.promptt001.coordinatecracker.io.PatternCodec;
import io.github.promptt001.coordinatecracker.io.PatternData;
import io.github.promptt001.coordinatecracker.io.TextureManager;
import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector2;
import io.github.promptt001.coordinatecracker.math.Vector3;
import io.github.promptt001.coordinatecracker.utils.MathHelper;

public final class Main implements ActionListener {

	private static EnumMode programMode;

	private static final int VERSION_MAJOR = 7;
	private static final int VERSION_MINOR = 12111;
	private static final int PATTERN_SIZE = 7;
	private static final int PATTERN_CENTER = PATTERN_SIZE / 2;
	private static final int TILE_SIZE = 54;
	private static final int TILE_ICON_PADDING = 8;
	private static final int DEFAULT_MAX_MATCHES = 100;
	private static final int RESULT_VIEWER_ROW_LIMIT = 10_000;
	private static final long RESULT_FLUSH_INTERVAL_MS = 250L;
	private static final DecimalFormat ONE_DECIMAL_FORMAT = new DecimalFormat("0.0");

	private static final int masterArgsMin = 1;
	private static final int slaveArgsMin = 2;

	private static int PORT;
	private static int MAXCONS;
	private static String KEY;
	private static String MASTERHOST;

	private static Main instance;

	private JFrame frame;
	private List<JButton> fieldButtons;
	private JPanel patternGrid;

	private JButton btnStartCracking;
	private JButton btnRotateRight;
	private JButton btnRotateLeft;
	private JButton btnSetTextured;
	private JButton btnClearTile;
	private JButton btnClearLayer;
	private JButton btnClearPattern;
	private JButton btnLoadPatternFile;
	private JButton btnChooseFilename;
	private JButton btnChooseTextureSource;
	private JButton btnApply;

	private JComboBox<EnumBlockType> comboBoxBlockType;
	private JComboBox<String> comboBoxRotation;
	private JComboBox<EnumAccelerationMode> comboBoxAcceleration;
	private JComboBox<String> comboBoxSurface;

	private JTextField saveResultsField;
	private JTextField radiusTextField;
	private JTextField maxMatchesTextField;

	private JSpinner layerSpinner;
	private JSpinner ySpinnerMin;
	private JSpinner ySpinnerMax;

	private JLabel lblProgress;
	private JLabel lblTotalMatches;
	private JLabel lblSpeed;
	private JLabel lblEta;
	private JLabel lblScanVolume;
	private JLabel lblSearchEstimate;
	private JLabel lblResultsViewerStatus;
	private JLabel lblSelectedTile;
	private JLabel lblPatternSummary;
	private JLabel lblSavePathStatus;
	private JLabel lblTextureStatus;
	private JLabel lblPatternWarning;

	private Vector2 activeField;
	private Matrix3 pattern;
	private Matrix3 blockTypes;

	private JTable resultsTable;
	private DefaultTableModel resultsTableModel;
	private JButton btnCopyTeleport;
	private JButton btnClearResultsViewer;
	private Timer resultFlushTimer;
	private final Object pendingResultRowsLock = new Object();
	private List<ResultRow> pendingResultRows;
	private int resultsSeenCount;
	private int displayedResultCount;

	private boolean crackerRunning;
	private boolean crackerStopping;
	private boolean crackerEnabled;
	private boolean validYMin;
	private boolean validYMax;
	private boolean validRadius;
	private boolean validMaxMatches;
	private boolean patternSurfaceValid;

	private int currentLayer;
	private boolean syncingSelectionControls;
	private EnumBlockType brushBlockType;
	private TextureManager textureManager;
	private TileIconRenderer tileIconRenderer;

	private int yMin;
	private int yMax;
	private int radius;
	private int maxMatches;
	private int threadCount;

	private String saveResultsPath;
	private CoordinateBruteforcer cb;
	private EnumMCVersion mcVersion;
	private EnumRotation rotation;
	private EnumAccelerationMode accelerationMode;

	/** Main entrypoint. */
	public static void main(String[] args) {
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
		programMode = EnumMode.SINGLE;

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("master") || args[0].equalsIgnoreCase("--master")) {
				int argsSpecified = 0;

				String port = parseArgument(arguments, "port");
				if(!port.isEmpty()) {
					PORT = (MathHelper.isInteger(port) ? Integer.valueOf(port) : -1);
					if(PORT > 0) ++argsSpecified;
					System.out.println("Port: " + port);
				}

				String key = parseArgument(arguments, "key");
				if(!key.isEmpty()) {
					KEY = key;
					System.out.println("Key: " + key);
				}

				String maxcons = parseArgument(arguments, "maxcons");
				if(!maxcons.isEmpty()) {
					MAXCONS = (MathHelper.isInteger(maxcons) ? Integer.valueOf(maxcons) : -1);
					System.out.println("Maximum connections: " + maxcons);
				}

				if(argsSpecified >= masterArgsMin) programMode = EnumMode.MASTER;
				else showMasterHelp(EnumReason.COUNT);
			}
			else if(args[0].equalsIgnoreCase("slave") || args[0].equalsIgnoreCase("--slave")) {
				int argsSpecified = 0;

				String host = parseArgument(arguments, "host");
				if(!host.isEmpty()) {
					if(host.matches("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b") || host.matches("^[a-zA-Z0-9.-]+$")) {
						MASTERHOST = host;
						++argsSpecified;
					}
					System.out.println("Host: " + host);
				}

				String port = parseArgument(arguments, "port");
				if(!port.isEmpty()) {
					PORT = (MathHelper.isInteger(port) ? Integer.valueOf(port) : -1);
					if(PORT > 0) ++argsSpecified;
					System.out.println("Port: " + port);
				}

				String key = parseArgument(arguments, "key");
				if(!key.isEmpty()) {
					KEY = key;
					System.out.println("Key: " + key);
				}

				if(argsSpecified >= slaveArgsMin) programMode = EnumMode.SLAVE;
				else showSlaveHelp(EnumReason.COUNT);
			}
			else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("--help")) {
				showHelpMenu();
			}
			else showHelpMenu();
		}

		if(programMode == EnumMode.SINGLE) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Main window = new Main();
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
						new ErrorPopup().showPopup();
					}
				}
			});
		}

		if(programMode == EnumMode.MASTER) {
			System.out.println("Starting master on port: " + PORT + " (network mode is not implemented in this fork).");
		}

		if(programMode == EnumMode.SLAVE) {
			System.out.println("Connecting to master " + MASTERHOST + ":" + PORT + " (network mode is not implemented in this fork).");
		}
	}

	public Main() {
		initialize();
	}

	private static String parseArgument(ArrayList<String> arguments, String argument) {
		if(arguments.contains(argument) || arguments.contains("--" + argument)) {
			try {
				int index = arguments.indexOf(argument);
				if(index < 0) index = arguments.indexOf("--" + argument);
				if(arguments.size() > index + 1) {
					return arguments.get(index + 1);
				}
			} catch(Exception e) {
				return "";
			}
		}
		return "";
	}

	private static void showHelpMenu() {
		System.out.println("-----[ HELP MENU ]-----");
		System.out.println("Run without arguments to start in single mode.");
		System.out.println("");
		System.out.println("Arguments:");
		System.out.println(" --help        : Shows this help menu");
		System.out.println(" --master      : Placeholder for future distributed cracking mode");
		System.out.println(" --slave       : Placeholder for future distributed cracking mode");
		programMode = EnumMode.DIE;
	}

	private static void showMasterHelp(EnumReason reason) {
		System.out.println("-----[ MASTER MODE HELP MENU ]-----");
		System.out.println(reason == EnumReason.NONE ? "" : reason.description + "\n");
		System.out.println("Arguments:");
		System.out.println(" --port <port> : required : port for worker connections");
		System.out.println(" --key <key>   : optional : shared worker key");
		System.out.println(" --maxcons <n> : optional : maximum workers");
		programMode = EnumMode.DIE;
	}

	private static void showSlaveHelp(EnumReason reason) {
		System.out.println("-----[ SLAVE HELP MENU ]-----");
		System.out.println(reason == EnumReason.NONE ? "" : reason.description + "\n");
		System.out.println("Arguments:");
		System.out.println(" --host <host> : required : master host address");
		System.out.println(" --port <port> : required : master port");
		System.out.println(" --key <key>   : optional : shared worker key");
		programMode = EnumMode.DIE;
	}

	private void initialize() {
		instance = this;

		this.fieldButtons = new ArrayList<>();
		this.activeField = new Vector2(PATTERN_CENTER, PATTERN_CENTER);
		this.pattern = new Matrix3(new Vector3(PATTERN_SIZE, PATTERN_SIZE, PATTERN_SIZE), new Vector3(PATTERN_CENTER, PATTERN_CENTER, PATTERN_CENTER));
		this.blockTypes = new Matrix3(new Vector3(PATTERN_SIZE, PATTERN_SIZE, PATTERN_SIZE), new Vector3(PATTERN_CENTER, PATTERN_CENTER, PATTERN_CENTER));
		this.brushBlockType = EnumBlockType.DEEPSLATE;
		this.textureManager = new TextureManager();
		this.textureManager.loadInitialSource();
		this.tileIconRenderer = new TileIconRenderer(this.textureManager);
		this.pendingResultRows = new ArrayList<ResultRow>();
		this.syncingSelectionControls = false;
		clearEntirePattern(false);

		this.currentLayer = PATTERN_CENTER + 1;

		this.cb = null;
		this.crackerEnabled = true;
		this.yMin = -64;
		this.yMax = 320;
		this.radius = 100;
		this.maxMatches = DEFAULT_MAX_MATCHES;
		this.validYMin = true;
		this.validYMax = true;
		this.validRadius = true;
		this.validMaxMatches = true;
		this.patternSurfaceValid = true;
		this.threadCount = 1;
		this.rotation = EnumRotation.R_ALL;
		this.mcVersion = EnumMCVersion.V1_21_11;
		this.accelerationMode = EnumAccelerationMode.CPU;

		frame = new JFrame("Promptt Coordinate Cracker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(1120, 720));
		frame.setLocationByPlatform(true);

		JPanel root = new JPanel(new BorderLayout(12, 12));
		root.setBorder(new EmptyBorder(12, 12, 12, 12));
		frame.getContentPane().add(root);

		root.add(createHeaderPanel(), BorderLayout.NORTH);
		root.add(createPatternEditorPanel(), BorderLayout.CENTER);
		root.add(createControlPanel(), BorderLayout.EAST);

		this.renderLayer(this.currentLayer);
		this.updatePatternSummary();
		this.updateSelectedTileLabel();
		this.validateStartButton();

		frame.pack();
	}

	private JPanel createHeaderPanel() {
		JPanel header = new JPanel(new BorderLayout(8, 4));
		JLabel title = new JLabel("Block Rotation Coordinate Cracker");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
		JLabel subtitle = new JLabel("1.21.11 workflow: mark each visible block's type and rotation/variant, set search bounds, then scan for matching coordinates.");
		subtitle.setForeground(new Color(85, 85, 85));
		header.add(title, BorderLayout.NORTH);
		header.add(subtitle, BorderLayout.SOUTH);
		return header;
	}

	private JPanel createPatternEditorPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(createTitledBorder("1. Build the observed pattern"));

		JPanel topRow = new JPanel(new BorderLayout(8, 8));
		JPanel layerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = constraints();
		c.gridx = 0;
		c.gridy = 0;
		layerPanel.add(new JLabel("Pattern layer:"), c);
		c.gridx = 1;
		SpinnerModel values = new SpinnerNumberModel(this.currentLayer, 1, PATTERN_SIZE, 1);
		layerSpinner = new JSpinner(values);
		layerSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				currentLayer = (int) layerSpinner.getValue();
				renderLayer(currentLayer);
				updateSelectedTileLabel();
			}
		});
		layerPanel.add(layerSpinner, c);
		c.gridx = 2;
		layerPanel.add(new JLabel("Layer 4 is the visible plane; other layers are depth/height offsets for the selected surface mode."), c);
		topRow.add(layerPanel, BorderLayout.WEST);

		lblPatternSummary = new JLabel();
		lblPatternSummary.setHorizontalAlignment(SwingConstants.RIGHT);
		topRow.add(lblPatternSummary, BorderLayout.EAST);
		panel.add(topRow, BorderLayout.NORTH);

		patternGrid = new SquareGridPanel(PATTERN_SIZE, 2);
		patternGrid.setBorder(new EmptyBorder(8, 8, 8, 8));
		patternGrid.setPreferredSize(new Dimension(PATTERN_SIZE * TILE_SIZE, PATTERN_SIZE * TILE_SIZE));
		patternGrid.setMinimumSize(new Dimension(PATTERN_SIZE * 34, PATTERN_SIZE * 34));
		patternGrid.setBackground(new Color(40, 40, 40));
		patternGrid.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						renderLayer(currentLayer);
					}
				});
			}
		});

		for(int z = 0; z < PATTERN_SIZE; z++) {
			for(int x = 0; x < PATTERN_SIZE; x++) {
				JButton button = new JButton("");
				button.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
				button.setMinimumSize(new Dimension(30, 30));
				button.setMargin(new Insets(0, 0, 0, 0));
				button.setFocusPainted(false);
				button.setContentAreaFilled(false);
				button.setOpaque(false);
				button.setHorizontalTextPosition(SwingConstants.CENTER);
				button.setVerticalTextPosition(SwingConstants.CENTER);
				button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
				button.putClientProperty("x", x);
				button.putClientProperty("z", z);
				button.addActionListener(this);
				button.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						if(SwingUtilities.isRightMouseButton(e) && !crackerRunning) {
							JButton b = (JButton) e.getSource();
							selectTile((Integer) b.getClientProperty("x"), (Integer) b.getClientProperty("z"));
							setTileValue(activeField.getX(), currentLayer - 1, activeField.getZ(), 4);
							renderLayer(currentLayer);
						}
					}
				});
				this.fieldButtons.add(button);
				patternGrid.add(button);
			}
		}

		JPanel gridWrap = new JPanel(new BorderLayout());
		gridWrap.add(patternGrid, BorderLayout.CENTER);
		panel.add(gridWrap, BorderLayout.CENTER);

		JPanel editorControls = new JPanel(new GridBagLayout());
		GridBagConstraints ec = constraints();
		ec.gridx = 0;
		ec.gridy = 0;
		ec.gridwidth = 4;
		lblSelectedTile = new JLabel();
		editorControls.add(lblSelectedTile, ec);

		ec.gridwidth = 1;
		ec.gridy = 1;
		ec.gridx = 0;
		editorControls.add(new JLabel("Block type:"), ec);
		ec.gridx = 1;
		comboBoxBlockType = new JComboBox<EnumBlockType>(EnumBlockType.selectableValues());
		comboBoxBlockType.setRenderer(new BlockTypeListCellRenderer());
		comboBoxBlockType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(syncingSelectionControls || crackerRunning) return;
				EnumBlockType selected = (EnumBlockType) comboBoxBlockType.getSelectedItem();
				if(selected == null) selected = EnumBlockType.DEEPSLATE;
				brushBlockType = selected;
				Vector3 pos = activeMatrixPos();
				setTileBlockType(pos.getX(), pos.getY(), pos.getZ(), selected);
				clampTileValueToBlockType(pos, selected);
				renderLayer(currentLayer);
			}
		});
		editorControls.add(comboBoxBlockType, ec);

		ec.gridx = 2;
		editorControls.add(new JLabel("Rotation / variant:"), ec);
		ec.gridx = 3;
		comboBoxRotation = new JComboBox<String>(new String[] {"unknown", "0", "1", "2", "3"});
		comboBoxRotation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(syncingSelectionControls || crackerRunning) return;
				Vector3 pos = activeMatrixPos();
				Object selected = comboBoxRotation.getSelectedItem();
				int value = selected == null || selected.toString().equals("unknown") ? 4 : Integer.valueOf(selected.toString());
				setTileBlockType(pos.getX(), pos.getY(), pos.getZ(), brushBlockType);
				if(value != 4 && value >= brushBlockType.getGuiStateCount()) {
					showError("Unsupported value for block", brushBlockType.getDisplayName() + " only supports GUI value 0" + (brushBlockType.getGuiStateCount() > 1 ? ".." + (brushBlockType.getGuiStateCount() - 1) : "") + ".");
					value = 0;
				}
				setTileValue(pos.getX(), pos.getY(), pos.getZ(), value);
				renderLayer(currentLayer);
			}
		});
		editorControls.add(comboBoxRotation, ec);

		ec.gridx = 0;
		ec.gridy = 2;
		btnSetTextured = new JButton("Toggle known/unknown");
		btnSetTextured.addActionListener(this);
		editorControls.add(btnSetTextured, ec);

		ec.gridx = 1;
		btnRotateLeft = new JButton("Rotation -");
		btnRotateLeft.addActionListener(this);
		editorControls.add(btnRotateLeft, ec);

		ec.gridx = 2;
		btnRotateRight = new JButton("Rotation +");
		btnRotateRight.addActionListener(this);
		editorControls.add(btnRotateRight, ec);

		ec.gridx = 3;
		btnClearTile = new JButton("Clear tile");
		btnClearTile.addActionListener(this);
		editorControls.add(btnClearTile, ec);

		ec.gridx = 0;
		ec.gridy = 3;
		btnClearLayer = new JButton("Clear current layer");
		btnClearLayer.addActionListener(this);
		editorControls.add(btnClearLayer, ec);

		ec.gridx = 1;
		btnClearPattern = new JButton("Clear all layers");
		btnClearPattern.addActionListener(this);
		editorControls.add(btnClearPattern, ec);

		ec.gridx = 2;
		ec.gridwidth = 2;
		editorControls.add(createLegendPanel(), ec);

		ec.gridx = 0;
		ec.gridy = 4;
		ec.gridwidth = 4;
		lblPatternWarning = new JLabel(" ");
		lblPatternWarning.setForeground(Color.RED);
		lblPatternWarning.setFont(lblPatternWarning.getFont().deriveFont(Font.BOLD));
		editorControls.add(lblPatternWarning, ec);

		panel.add(editorControls, BorderLayout.SOUTH);
		return panel;
	}


	private JPanel createLegendPanel() {
		JPanel legend = new JPanel(new GridLayout(2, 1));
		legend.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
		legend.add(new JLabel("Left-click cycles the selected tile's rotation. Unknown tiles use the current block-type brush."));
		legend.add(new JLabel("Use the dropdowns to change the selected tile's block type and rotation; right-click clears."));
		return legend;
	}

	private JPanel createControlPanel() {
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.setPreferredSize(new Dimension(360, 640));

		controls.add(createQuickGuidePanel());
		controls.add(Box.createVerticalStrut(10));
		controls.add(createSearchSettingsPanel());
		controls.add(Box.createVerticalStrut(10));
		controls.add(createPatternFilesPanel());
		controls.add(Box.createVerticalStrut(10));
		controls.add(createRunPanel());
		controls.add(Box.createVerticalStrut(10));
		controls.add(createResultsViewerPanel());

		JScrollPane scroll = new JScrollPane(controls);
		scroll.setBorder(null);
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(scroll, BorderLayout.CENTER);
		return wrapper;
	}

	private JPanel createQuickGuidePanel() {
		JPanel panel = new JPanel(new BorderLayout(6, 6));
		panel.setBorder(createTitledBorder("Workflow"));
		JTextArea guide = new JTextArea(
			"1. Choose a reference block in the screenshot. Put it on the center tile of layer 4.\n" +
			"2. For each visible observation, set the block type and its rotation/variant. Unknown tiles are ignored.\n" +
			"3. Included 1.21.11 presets now cover common terrain, cave blocks, and bedrock.\n" +
			"4. Set Surface to exactly one observed plane: wall/side, floor/top, or ceiling/bottom. Use Facing to try all directions or lock one.\n" +
			"5. Keep Y and radius tight for faster, cleaner results."
		);
		guide.setEditable(false);
		guide.setLineWrap(true);
		guide.setWrapStyleWord(true);
		guide.setOpaque(false);
		panel.add(guide, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createSearchSettingsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(createTitledBorder("2. Search settings"));
		GridBagConstraints c = constraints();

		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("Threads:"), c);
		c.gridx = 1;
		DefaultComboBoxModel<String> threadModel = new DefaultComboBoxModel<String>();
		int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
		for(int t = 1; t <= maxThreads; t *= 2) {
			threadModel.addElement(String.valueOf(t));
		}
		if((maxThreads & (maxThreads - 1)) != 0) {
			threadModel.addElement(String.valueOf(maxThreads));
		}
		final JComboBox<String> comboBoxThreads = new JComboBox<String>(threadModel);
		comboBoxThreads.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selected = comboBoxThreads.getSelectedItem();
				if(selected != null) threadCount = Integer.valueOf(selected.toString());
			}
		});
		panel.add(comboBoxThreads, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("Acceleration:"), c);
		c.gridx = 1;
		comboBoxAcceleration = new JComboBox<EnumAccelerationMode>(EnumAccelerationMode.values());
		comboBoxAcceleration.setToolTipText("GPU mode uses the optional OpenCL helper. Configure it with -Dcoordinatecracker.gpuCommand=/path/to/coordinatecracker-opencl-helper.");
		comboBoxAcceleration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EnumAccelerationMode selected = (EnumAccelerationMode) comboBoxAcceleration.getSelectedItem();
				accelerationMode = selected == null ? EnumAccelerationMode.CPU : selected;
			}
		});
		panel.add(comboBoxAcceleration, c);

		c.gridx = 0;
		c.gridy = 2;
		panel.add(new JLabel("MC version:"), c);
		c.gridx = 1;
		final JComboBox<String> comboBoxMcVersion = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] {"1.21.11", "1.16.5", "1.12.2"}));
		comboBoxMcVersion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selected = comboBoxMcVersion.getSelectedItem();
				if(selected == null) return;
				String selectedItem = selected.toString();
				if(selectedItem.equals("1.21.11")) mcVersion = EnumMCVersion.V1_21_11;
				else if(selectedItem.equals("1.16.5")) mcVersion = EnumMCVersion.V1_16_5;
				else if(selectedItem.equals("1.12.2")) mcVersion = EnumMCVersion.V1_12_2;
			}
		});
		panel.add(comboBoxMcVersion, c);

		c.gridx = 0;
		c.gridy = 3;
		panel.add(new JLabel("Surface:"), c);
		c.gridx = 1;
		comboBoxSurface = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] {
			"Wall / side",
			"Floor / top",
			"Ceiling / bottom"
		}));
		comboBoxSurface.setToolTipText("Choose exactly one visible surface. The app will warn if the pattern mixes side/top/bottom face profiles.");
		panel.add(comboBoxSurface, c);

		c.gridx = 0;
		c.gridy = 4;
		panel.add(new JLabel("Facing:"), c);
		c.gridx = 1;
		final JComboBox<String> comboBoxFacing = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] {
			"try all selected facings",
			"north",
			"east",
			"south",
			"west"
		}));
		ActionListener surfaceSelectionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSurfaceRotation(comboBoxSurface, comboBoxFacing);
			}
		};
		comboBoxSurface.addActionListener(surfaceSelectionListener);
		comboBoxFacing.addActionListener(surfaceSelectionListener);
		panel.add(comboBoxFacing, c);

		c.gridx = 0;
		c.gridy = 5;
		panel.add(new JLabel("Y min:"), c);
		c.gridx = 1;
		SpinnerModel ySpinnerMinValues = new SpinnerNumberModel(this.yMin, -64, 319, 1);
		ySpinnerMin = new JSpinner(ySpinnerMinValues);
		ySpinnerMin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				spinnerStateChangedHandler(arg0);
			}
		});
		panel.add(ySpinnerMin, c);

		c.gridx = 0;
		c.gridy = 6;
		panel.add(new JLabel("Y max:"), c);
		c.gridx = 1;
		SpinnerModel ySpinnerMaxValues = new SpinnerNumberModel(this.yMax, -63, 320, 1);
		ySpinnerMax = new JSpinner(ySpinnerMaxValues);
		ySpinnerMax.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				spinnerStateChangedHandler(arg0);
			}
		});
		panel.add(ySpinnerMax, c);

		c.gridx = 0;
		c.gridy = 7;
		panel.add(new JLabel("Radius:"), c);
		c.gridx = 1;
		radiusTextField = new JTextField(String.valueOf(this.radius), 10);
		radiusTextField.setToolTipText("Zero scans only the world origin; positive values scan -radius through +radius on X/Z.");
		radiusTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) { validateContent(); }
			@Override public void insertUpdate(DocumentEvent arg0) { validateContent(); }
			@Override public void removeUpdate(DocumentEvent arg0) { validateContent(); }

			private void validateContent() {
				if(MathHelper.isInteger(radiusTextField.getText())) {
					int r = Integer.valueOf(radiusTextField.getText());
					if(r >= 0) {
						validRadius = true;
						radius = r;
						radiusTextField.setForeground(null);
					} else markError();
				} else markError();
				updateSearchEstimateLabel();
				validateStartButton();
			}

			private void markError() {
				validRadius = false;
				radiusTextField.setForeground(Color.RED);
			}
		});
		panel.add(radiusTextField, c);

		c.gridx = 0;
		c.gridy = 8;
		panel.add(new JLabel("Max matches:"), c);
		c.gridx = 1;
		maxMatchesTextField = new JTextField(String.valueOf(this.maxMatches), 10);
		maxMatchesTextField.setToolTipText("Abort the scan after this many accepted matches. Use 0 for unlimited.");
		maxMatchesTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) { validateContent(); }
			@Override public void insertUpdate(DocumentEvent arg0) { validateContent(); }
			@Override public void removeUpdate(DocumentEvent arg0) { validateContent(); }

			private void validateContent() {
				if(MathHelper.isInteger(maxMatchesTextField.getText())) {
					int value = Integer.valueOf(maxMatchesTextField.getText());
					if(value >= 0) {
						validMaxMatches = true;
						maxMatches = value;
						maxMatchesTextField.setForeground(null);
					} else markError();
				} else markError();
				validateStartButton();
			}

			private void markError() {
				validMaxMatches = false;
				maxMatchesTextField.setForeground(Color.RED);
			}
		});
		panel.add(maxMatchesTextField, c);

		c.gridx = 0;
		c.gridy = 9;
		c.gridwidth = 2;
		lblSearchEstimate = new JLabel();
		lblSearchEstimate.setToolTipText("Heuristic only: assumes each known observation filters independently by its visible state count.");
		panel.add(lblSearchEstimate, c);
		c.gridwidth = 1;

		c.gridx = 0;
		c.gridy = 10;
		panel.add(new JLabel("Results file:"), c);
		c.gridx = 1;
		saveResultsField = new JTextField("", 16);
		saveResultsField.setToolTipText("Leave blank to create coordinates_yyyy-MM-dd_HH-mm-ss.txt");
		panel.add(saveResultsField, c);

		c.gridx = 0;
		c.gridy = 11;
		btnApply = new JButton("Use file name");
		btnApply.addActionListener(this);
		panel.add(btnApply, c);
		c.gridx = 1;
		lblSavePathStatus = new JLabel("Blank = auto-name");
		panel.add(lblSavePathStatus, c);

		return panel;
	}

	private void updateSurfaceRotation(JComboBox<String> comboBoxSurface, JComboBox<String> comboBoxFacing) {
		int surfaceIndex = comboBoxSurface == null ? 0 : comboBoxSurface.getSelectedIndex();
		boolean walls = surfaceIndex <= 0;
		boolean floors = surfaceIndex == 1;
		boolean ceilings = surfaceIndex == 2;

		int selectedIndex = comboBoxFacing.getSelectedIndex();
		boolean allFacings = selectedIndex <= 0;
		int yawDegrees;
		switch(selectedIndex) {
		case 2:
			yawDegrees = 90;
			break;
		case 3:
			yawDegrees = 180;
			break;
		case 4:
			yawDegrees = 270;
			break;
		case 1:
		case 0:
		default:
			yawDegrees = 0;
			break;
		}

		this.rotation = EnumRotation.fromSurfaceSelection(walls, floors, ceilings, yawDegrees, allFacings);
		updateSearchEstimateLabel();
		updatePatternSurfaceWarning();
		validateStartButton();
	}

	private JPanel createPatternFilesPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(createTitledBorder("Pattern files"));
		GridBagConstraints c = constraints();

		c.gridx = 0;
		c.gridy = 0;
		btnLoadPatternFile = new JButton("Load pattern...");
		btnLoadPatternFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(crackerRunning) return;
				JFileChooser fileChooser = new JFileChooser();
				int action = fileChooser.showOpenDialog(frame);
				if(action == JFileChooser.APPROVE_OPTION) {
					File loadFile = fileChooser.getSelectedFile();
					applyPatternFromFile(loadFile);
				}
			}
		});
		panel.add(btnLoadPatternFile, c);

		c.gridx = 1;
		btnChooseFilename = new JButton("Save pattern...");
		btnChooseFilename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(crackerRunning) return;
				JFileChooser fileChooser = new JFileChooser();
				int action = fileChooser.showSaveDialog(frame);
				if(action == JFileChooser.APPROVE_OPTION) {
					File saveFile = fileChooser.getSelectedFile();
					savePatternToFile(saveFile);
				}
			}
		});
		panel.add(btnChooseFilename, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		lblTextureStatus = new JLabel(textureManager.getStatusMessage());
		panel.add(lblTextureStatus, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		btnChooseTextureSource = new JButton("Choose texture source...");
		btnChooseTextureSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(crackerRunning) return;
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setDialogTitle("Choose a resource-pack zip, licensed texture pack, or assets folder");
				int action = fileChooser.showOpenDialog(frame);
				if(action == JFileChooser.APPROVE_OPTION) {
					File source = fileChooser.getSelectedFile();
					textureManager.loadUserSource(source);
					updateTextureStatusLabel();
					renderLayer(currentLayer);
				}
			}
		});
		panel.add(btnChooseTextureSource, c);

		return panel;
	}

	private void updateTextureStatusLabel() {
		if(lblTextureStatus != null) {
			lblTextureStatus.setText(textureManager.getStatusMessage());
			lblTextureStatus.setToolTipText(textureManager.getSourceDescription());
		}
	}

	private JPanel createRunPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setBorder(createTitledBorder("3. Run"));

		btnStartCracking = new JButton("Start scan");
		btnStartCracking.setBackground(new Color(80, 180, 90));
		btnStartCracking.setOpaque(true);
		btnStartCracking.addActionListener(this);
		panel.add(btnStartCracking, BorderLayout.NORTH);

		JPanel status = new JPanel(new GridLayout(5, 1, 4, 4));
		lblProgress = new JLabel("Progress: 0.0%");
		lblTotalMatches = new JLabel("Total matches: 0");
		lblSpeed = new JLabel("Speed: —");
		lblEta = new JLabel("ETA: —");
		lblScanVolume = new JLabel("Checked: 0 / 0 candidates");
		status.add(lblProgress);
		status.add(lblTotalMatches);
		status.add(lblSpeed);
		status.add(lblEta);
		status.add(lblScanVolume);
		panel.add(status, BorderLayout.CENTER);

		JTextArea note = new JTextArea("Matches are appended to the selected results file. The reported coordinate is the center/reference block represented by the center tile on depth layer 4.");
		note.setEditable(false);
		note.setLineWrap(true);
		note.setWrapStyleWord(true);
		note.setOpaque(false);
		panel.add(note, BorderLayout.SOUTH);

		return panel;
	}


	private JPanel createResultsViewerPanel() {
		JPanel panel = new JPanel(new BorderLayout(6, 6));
		panel.setBorder(createTitledBorder("Results viewer"));

		resultsTableModel = new DefaultTableModel(new Object[] {"#", "X", "Y", "Z", "Facing", "Dist²"}, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		resultsTable = new JTable(resultsTableModel);
		resultsTable.setAutoCreateRowSorter(true);
		resultsTable.setFillsViewportHeight(true);
		resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(resultsTable);
		scrollPane.setPreferredSize(new Dimension(320, 180));
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel actions = new JPanel(new BorderLayout(6, 6));
		JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
		btnCopyTeleport = new JButton("Copy /tp");
		btnCopyTeleport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copySelectedTeleportCommand();
			}
		});
		buttons.add(btnCopyTeleport);

		btnClearResultsViewer = new JButton("Clear viewer");
		btnClearResultsViewer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearResultsViewer(false);
			}
		});
		buttons.add(btnClearResultsViewer);
		actions.add(buttons, BorderLayout.NORTH);

		lblResultsViewerStatus = new JLabel("No results yet. Results are also written to the selected file.");
		actions.add(lblResultsViewerStatus, BorderLayout.SOUTH);
		panel.add(actions, BorderLayout.SOUTH);
		return panel;
	}

	private GridBagConstraints constraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 4, 4, 4);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		return c;
	}

	private TitledBorder createTitledBorder(String title) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
		return border;
	}

	/**
	 * Repaint the visible 7x7 layer from the rotation matrix and block-type matrix.
	 * Unknown cells stay ignored by the cracker, but keep a brush block type so a
	 * subsequent click knows which block preset to apply.
	 */
	private void renderLayer(int layer) {
		int layerIndex = layer - 1;
		for(JButton b : this.fieldButtons) {
			int x = (Integer) b.getClientProperty("x");
			int z = (Integer) b.getClientProperty("z");
			Vector3 pos = new Vector3(x, layerIndex, z);
			EnumBlockType blockType = EnumBlockType.fromId(this.blockTypes.getValue(pos));
			this.setIconNumber(b, blockType, this.pattern.getValue(pos));

			if(x == this.activeField.getX() && z == this.activeField.getZ()) {
				b.setBorder(BorderFactory.createLineBorder(new Color(25, 110, 220), 3));
			} else if(x == PATTERN_CENTER && z == PATTERN_CENTER && layerIndex == PATTERN_CENTER) {
				b.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
			} else {
				b.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1));
			}
		}
		updateSelectedTileLabel();
		updatePatternSummary();
	}

	/**
	 * Load the strict plain-text 7x7x7 pattern format. Unknown tokens are
	 * represented by value 4 in the rotation matrix and are skipped during
	 * matching.
	 */
	private void applyPatternFromFile(File file) {
		try {
			PatternData data = PatternCodec.load(file, PATTERN_SIZE, PATTERN_CENTER);
			Matrix3 tmpPattern = data.getPatternMatrix();
			Matrix3 tmpBlockTypes = data.getBlockTypeMatrix();
			for(int y = 0; y < PATTERN_SIZE; ++y) {
				this.pattern.setLayer(y, tmpPattern.getLayer(y));
				this.blockTypes.setLayer(y, tmpBlockTypes.getLayer(y));
			}

			this.renderLayer(this.currentLayer);
			showInfo("Pattern loaded", "Loaded pattern from:\n" + file.getAbsolutePath());
		} catch (Exception e) {
			showError("Could not load pattern", e.getMessage());
		}
	}

	private void savePatternToFile(File file) {
		try {
			PatternCodec.save(file, this.pattern, this.blockTypes, PATTERN_SIZE);
			showInfo("Pattern saved", "Saved pattern to:\n" + file.getAbsolutePath());
		} catch (Exception e) {
			showError("Could not save pattern", e.getMessage());
		}
	}


	private void setIconNumber(JButton buttonIn, EnumBlockType blockType, int icon) {
		EnumBlockType safeBlockType = blockType == null ? EnumBlockType.DEEPSLATE : blockType;
		int iconSize = getDynamicTileIconSize(buttonIn);

		buttonIn.setText("");
		buttonIn.setForeground(this.tileIconRenderer.shouldUseLightText(safeBlockType) ? Color.WHITE : Color.BLACK);
		buttonIn.setBackground(this.tileIconRenderer.colorForBlockType(safeBlockType));

		if(icon == 4) {
			buttonIn.setIcon(this.tileIconRenderer.createUnknownTileIcon(iconSize));
			buttonIn.setToolTipText("Unknown / ignored. Current tile block type: " + safeBlockType.getDisplayName() + " (" + safeBlockType.getSurfaceDescription() + ")");
			return;
		}

		buttonIn.setIcon(this.tileIconRenderer.createTileIcon(safeBlockType, icon, iconSize));
		String textureSource = textureManager.hasTexture(safeBlockType) ? textureManager.getSourceDescription() : "texture not loaded";
		buttonIn.setToolTipText(safeBlockType.getDisplayName() + " rotation/variant " + icon + " (" + safeBlockType.getSurfaceDescription() + ") — " + textureSource);
	}

	private int getDynamicTileIconSize(JButton button) {
		int width = button.getWidth() > 0 ? button.getWidth() : TILE_SIZE;
		int height = button.getHeight() > 0 ? button.getHeight() : TILE_SIZE;
		return Math.max(22, Math.min(width, height) - 6);
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(!(arg0.getSource() instanceof JButton)) return;
		JButton btnIn = (JButton) arg0.getSource();

		if(btnIn == this.btnStartCracking) {
			if(!this.crackerRunning) {
				updatePatternSurfaceWarning();
				if(!this.crackerEnabled || !this.validRadius || !this.validMaxMatches || !this.validYMin || !this.validYMax) {
					showError("Search settings need attention", "Fix the highlighted Y range or radius before starting.");
					return;
				}
				if(!this.patternSurfaceValid) {
					showError("Impossible pattern", this.lblPatternWarning == null ? "The selected pattern mixes incompatible side/top/bottom face profiles." : this.lblPatternWarning.getText());
					return;
				}
				if(countKnownTiles() == 0) {
					showError("Pattern is empty", "Mark at least one known block variant before starting a scan.");
					return;
				}

				this.crackingStart();
				ScanLaunchRequest request = new ScanLaunchRequest(
					this.pattern,
					this.blockTypes,
					this.radius,
					this.yMin,
					this.yMax,
					this.mcVersion,
					this.rotation,
					this.threadCount,
					this.accelerationMode,
					this.maxMatches
				);
				cb = request.createBruteforcer(this.getInstance());
				startScanInitializationThread(cb);
				return;
			}

			try {
				this.crackingStopping();
				if(cb != null) cb.requestCancel();
			} catch(Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if(this.crackerRunning) return;

		if(this.fieldButtons.contains(btnIn)) {
			int x = (Integer) btnIn.getClientProperty("x");
			int z = (Integer) btnIn.getClientProperty("z");
			this.activeField.setX(x);
			this.activeField.setZ(z);
			cycleTile(1);
			return;
		}

		if(btnIn == this.btnApply) {
			String requested = this.saveResultsField.getText().trim();
			this.saveResultsPath = requested.isEmpty() ? null : normalizeTxtPath(requested);
			this.lblSavePathStatus.setText(this.saveResultsPath == null ? "Blank = auto-name" : this.saveResultsPath);
			return;
		}

		if(btnIn == this.btnSetTextured) {
			Vector3 pos = activeMatrixPos();
			int current = this.pattern.getValue(pos);
			if(current == 4) setTileBlockType(pos.getX(), pos.getY(), pos.getZ(), this.brushBlockType);
			setTileValue(pos.getX(), pos.getY(), pos.getZ(), current == 4 ? 0 : 4);
			this.renderLayer(this.currentLayer);
			return;
		}

		if(btnIn == this.btnRotateRight) {
			cycleTile(1);
			return;
		}

		if(btnIn == this.btnRotateLeft) {
			cycleTile(-1);
			return;
		}

		if(btnIn == this.btnClearTile) {
			Vector3 pos = activeMatrixPos();
			setTileValue(pos.getX(), pos.getY(), pos.getZ(), 4);
			this.renderLayer(this.currentLayer);
			return;
		}

		if(btnIn == this.btnClearLayer) {
			clearCurrentLayer();
			this.renderLayer(this.currentLayer);
			return;
		}

		if(btnIn == this.btnClearPattern) {
			clearEntirePattern(true);
			this.renderLayer(this.currentLayer);
		}
	}

	private void startScanInitializationThread(final CoordinateBruteforcer scan) {
		Thread launcher = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					scan.load();
				} catch(RuntimeException e) {
					e.printStackTrace();
					showErrorAsync("Unable to start scan", e.getMessage());
					crackingEnd();
				} catch(Error e) {
					e.printStackTrace();
					showErrorAsync("Unable to start scan", e.getMessage());
					crackingEnd();
				}
			}
		}, "coordinatecracker-scan-initializer");
		launcher.setDaemon(true);
		launcher.start();
	}

	private void selectTile(int x, int z) {
		this.activeField.setX(x);
		this.activeField.setZ(z);
		this.renderLayer(this.currentLayer);
	}

	private Vector3 activeMatrixPos() {
		return new Vector3(this.activeField.getX(), this.currentLayer - 1, this.activeField.getZ());
	}

	private void cycleTile(int direction) {
		Vector3 pos = activeMatrixPos();
		int current = this.pattern.getValue(pos);
		EnumBlockType activeBlockType = EnumBlockType.fromId(this.blockTypes.getValue(pos));
		int stateCount = Math.max(1, activeBlockType.getGuiStateCount());
		int next;
		if(current == 4) {
			activeBlockType = this.brushBlockType;
			stateCount = Math.max(1, activeBlockType.getGuiStateCount());
			next = direction >= 0 ? 0 : stateCount - 1;
			setTileBlockType(pos.getX(), pos.getY(), pos.getZ(), activeBlockType);
		}
		else next = Math.floorMod(current + direction, stateCount);
		setTileValue(pos.getX(), pos.getY(), pos.getZ(), next);
		this.renderLayer(this.currentLayer);
	}

	private void setTileValue(int x, int y, int z, int value) {
		this.pattern.setValue(new Vector3(x, y, z), value);
	}

	private void setTileBlockType(int x, int y, int z, EnumBlockType blockType) {
		EnumBlockType safeBlockType = blockType == null ? EnumBlockType.DEEPSLATE : blockType;
		this.blockTypes.setValue(new Vector3(x, y, z), safeBlockType.getId());
	}

	private void clampTileValueToBlockType(Vector3 pos, EnumBlockType blockType) {
		int currentValue = this.pattern.getValue(pos);
		if(currentValue != 4 && currentValue >= blockType.getGuiStateCount()) {
			this.pattern.setValue(pos, 0);
		}
	}

	private void clearCurrentLayer() {
		int layer = this.currentLayer - 1;
		for(int x = 0; x < PATTERN_SIZE; x++) {
			for(int z = 0; z < PATTERN_SIZE; z++) {
				Vector3 pos = new Vector3(x, layer, z);
				this.pattern.setValue(pos, 4);
				this.blockTypes.setValue(pos, EnumBlockType.DEEPSLATE.getId());
			}
		}
	}

	private void clearEntirePattern(boolean keepSelection) {
		for(int x = 0; x < PATTERN_SIZE; ++x) {
			for(int y = 0; y < PATTERN_SIZE; ++y) {
				for(int z = 0; z < PATTERN_SIZE; ++z) {
					Vector3 pos = new Vector3(x, y, z);
					this.pattern.setValue(pos, 4);
					this.blockTypes.setValue(pos, EnumBlockType.DEEPSLATE.getId());
				}
			}
		}
		if(!keepSelection) this.activeField = new Vector2(PATTERN_CENTER, PATTERN_CENTER);
	}

	private int countKnownTiles() {
		int known = 0;
		for(int x = 0; x < PATTERN_SIZE; ++x) {
			for(int y = 0; y < PATTERN_SIZE; ++y) {
				for(int z = 0; z < PATTERN_SIZE; ++z) {
					if(this.pattern.getValue(new Vector3(x, y, z)) != 4) known++;
				}
			}
		}
		return known;
	}

	private void updatePatternSummary() {
		if(lblPatternSummary == null) return;
		int total = PATTERN_SIZE * PATTERN_SIZE * PATTERN_SIZE;
		int known = countKnownTiles();
		lblPatternSummary.setText("Known observations: " + known + " / " + total + " | Brush: " + this.brushBlockType.getDisplayName());
		updatePatternSurfaceWarning();
		updateSearchEstimateLabel();
		validateStartButton();
	}

	private void updatePatternSurfaceWarning() {
		PatternSurfaceStatus status = evaluatePatternSurfaceStatus();
		this.patternSurfaceValid = status.valid;
		if(this.lblPatternWarning != null) {
			this.lblPatternWarning.setText(status.message == null || status.message.isEmpty() ? " " : status.message);
		}
	}

	private PatternSurfaceStatus evaluatePatternSurfaceStatus() {
		int selectedSurfaceMask = getSelectedSurfaceMask();
		boolean hasSideProfile = false;
		boolean hasTopOnlyProfile = false;
		boolean hasBottomOnlyProfile = false;
		boolean hasHorizontalProfile = false;
		boolean hasIncompatibleProfile = false;
		String incompatibleDescription = null;

		for(int x = 0; x < PATTERN_SIZE; ++x) {
			for(int y = 0; y < PATTERN_SIZE; ++y) {
				for(int z = 0; z < PATTERN_SIZE; ++z) {
					Vector3 pos = new Vector3(x, y, z);
					if(this.pattern.getValue(pos) == 4) continue;

					EnumBlockType blockType = EnumBlockType.fromId(this.blockTypes.getValue(pos));
					int mask = blockType.getSurfaceMask();
					if(blockType.isFaceSpecific()) {
						boolean side = (mask & EnumBlockType.SURFACE_WALL) != 0;
						boolean top = (mask & EnumBlockType.SURFACE_FLOOR) != 0;
						boolean bottom = (mask & EnumBlockType.SURFACE_CEILING) != 0;
						if(side) hasSideProfile = true;
						if(top || bottom) hasHorizontalProfile = true;
						if(top && !bottom) hasTopOnlyProfile = true;
						if(bottom && !top) hasBottomOnlyProfile = true;
					}
					if(!blockType.isCompatibleWithSurface(selectedSurfaceMask)) {
						hasIncompatibleProfile = true;
						if(incompatibleDescription == null) incompatibleDescription = blockType.getDisplayName() + " (" + blockType.getSurfaceDescription() + ")";
					}
				}
			}
		}

		if(hasSideProfile && hasHorizontalProfile) {
			return new PatternSurfaceStatus(false, "WARNING: Impossible pattern — side-face and top/bottom-face block profiles are mixed. Pick one surface or clear/switch the conflicting tiles.");
		}
		if(hasTopOnlyProfile && hasBottomOnlyProfile) {
			return new PatternSurfaceStatus(false, "WARNING: Impossible pattern — top-face and bottom-face block profiles are mixed. Pick floor/top or ceiling/bottom, not both.");
		}
		if(hasIncompatibleProfile) {
			return new PatternSurfaceStatus(false, "WARNING: Impossible pattern — selected surface is " + getSelectedSurfaceLabel() + ", but the pattern contains " + incompatibleDescription + ".");
		}

		return new PatternSurfaceStatus(true, "");
	}

	private int getSelectedSurfaceMask() {
		if(this.rotation != null) {
			if(this.rotation.isFloor()) return EnumBlockType.SURFACE_FLOOR;
			if(this.rotation.isCeiling()) return EnumBlockType.SURFACE_CEILING;
		}
		return EnumBlockType.SURFACE_WALL;
	}

	private String getSelectedSurfaceLabel() {
		switch(getSelectedSurfaceMask()) {
		case EnumBlockType.SURFACE_FLOOR:
			return "Floor / top";
		case EnumBlockType.SURFACE_CEILING:
			return "Ceiling / bottom";
		case EnumBlockType.SURFACE_WALL:
		default:
			return "Wall / side";
		}
	}

	private static final class PatternSurfaceStatus {
		final boolean valid;
		final String message;

		PatternSurfaceStatus(boolean valid, String message) {
			this.valid = valid;
			this.message = message;
		}
	}

	private void updateSelectedTileLabel() {
		if(lblSelectedTile == null) return;
		Vector3 pos = activeMatrixPos();
		int value = this.pattern.getValue(pos);
		EnumBlockType blockType = EnumBlockType.fromId(this.blockTypes.getValue(pos));
		String valueText = value == 4 ? "unknown" : "variant " + value;
		int horizontalOffset = pos.getX() - PATTERN_CENTER;
		int verticalOffset = PATTERN_CENTER - pos.getZ();
		int depthOffset = pos.getY() - PATTERN_CENTER;
		lblSelectedTile.setText("Selected: horizontal offset " + horizontalOffset + ", vertical offset " + verticalOffset + ", depth offset " + depthOffset + " — " + blockType.getDisplayName() + " (" + blockType.getSurfaceDescription() + "), " + valueText);
		syncSelectionControls(blockType, value);
	}

	private void syncSelectionControls(EnumBlockType blockType, int rotationValue) {
		if(comboBoxBlockType == null || comboBoxRotation == null) return;
		this.syncingSelectionControls = true;
		this.brushBlockType = blockType;
		comboBoxBlockType.setSelectedItem(blockType);
		if(rotationValue == 4) comboBoxRotation.setSelectedItem("unknown");
		else comboBoxRotation.setSelectedItem(String.valueOf(rotationValue));
		this.syncingSelectionControls = false;
	}

	private void spinnerStateChangedHandler(ChangeEvent arg0) {
		if(!(arg0.getSource() instanceof JSpinner)) return;
		JSpinner jsp = (JSpinner) arg0.getSource();

		ySpinnerMax.getEditor().getComponent(0).setForeground(null);
		ySpinnerMin.getEditor().getComponent(0).setForeground(null);

		if(jsp == ySpinnerMin) {
			this.validYMin = true;
			if(Integer.valueOf(jsp.getValue().toString()) >= Integer.valueOf(ySpinnerMax.getValue().toString())) {
				jsp.getEditor().getComponent(0).setForeground(Color.RED);
				ySpinnerMax.getEditor().getComponent(0).setForeground(Color.RED);
				this.validYMin = false;
			} else this.yMin = Integer.valueOf(jsp.getValue().toString());
		}

		if(jsp == ySpinnerMax) {
			this.validYMax = true;
			if(Integer.valueOf(jsp.getValue().toString()) <= Integer.valueOf(ySpinnerMin.getValue().toString())) {
				jsp.getEditor().getComponent(0).setForeground(Color.RED);
				ySpinnerMin.getEditor().getComponent(0).setForeground(Color.RED);
				this.validYMax = false;
			} else this.yMax = Integer.valueOf(jsp.getValue().toString());
		}
		updateSearchEstimateLabel();
		validateStartButton();
	}

	private void validateStartButton() {
		if(btnStartCracking == null) return;
		boolean ok = this.crackerEnabled && this.validRadius && this.validMaxMatches && this.validYMin && this.validYMax && this.patternSurfaceValid && countKnownTiles() > 0 && !this.crackerRunning;
		btnStartCracking.setEnabled(ok || (this.crackerRunning && !this.crackerStopping));
	}

	public Main getInstance() {
		return instance;
	}

	public void crackingStart() {
		this.initializeSaveFile();
		this.lblSavePathStatus.setText(this.saveResultsPath);
		this.lblProgress.setText("Progress: 0.0%");
		this.lblTotalMatches.setText("Total matches: 0");
		this.lblSpeed.setText("Speed: —");
		this.lblEta.setText("ETA: calculating...");
		this.lblScanVolume.setText("Checked: 0 / " + formatCompactNumber(estimateCoordinateCandidates()) + " candidates");
		this.clearResultsViewer(true);
		this.startResultFlushTimer();
		this.btnStartCracking.setText("Stop scan");
		this.btnStartCracking.setBackground(new Color(205, 80, 80));
		this.crackerRunning = true;
		this.crackerStopping = false;
		this.validateStartButton();
	}

	public void crackingStopping() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				crackerStopping = true;
				btnStartCracking.setText("Stopping scan...");
				btnStartCracking.setBackground(new Color(160, 160, 160));
				validateStartButton();
			}
		});
	}

	public void crackingEnd() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				flushPendingResultRows();
				stopResultFlushTimer();
				btnStartCracking.setText("Start scan");
				btnStartCracking.setBackground(new Color(80, 180, 90));
				crackerRunning = false;
				crackerStopping = false;
				validateStartButton();
			}
		});
	}

	public void updateProgress(final int progressTenths) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setProgressLabel(progressTenths);
			}
		});
	}

	public void updateScanMetrics(final int progressTenths, final long completedIterations, final long totalIterations, final double candidatesPerSecond, final long etaSeconds) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setProgressLabel(progressTenths);
				lblSpeed.setText("Speed: " + formatRate(candidatesPerSecond));
				lblEta.setText("ETA: " + formatEta(etaSeconds));
				lblScanVolume.setText("Checked: " + formatCompactNumber(completedIterations) + " / " + formatCompactNumber(totalIterations) + " candidates");
			}
		});
	}

	private void setProgressLabel(int progressTenths) {
		int clamped = Math.max(0, Math.min(1000, progressTenths));
		lblProgress.setText("Progress: " + (clamped / 10) + "." + (clamped % 10) + "%");
	}

	public void updateMatchesCount(final int matches) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lblTotalMatches.setText("Total matches: " + formatCompactNumber(matches));
				updateResultsViewerStatus();
			}
		});
	}

	public void queueResultMatch(int x, int y, int z, String facing) {
		long distanceSquared = (long) x * (long) x + (long) z * (long) z;
		synchronized(this.pendingResultRowsLock) {
			this.resultsSeenCount++;
			if(this.resultsSeenCount <= RESULT_VIEWER_ROW_LIMIT) {
				this.pendingResultRows.add(new ResultRow(this.resultsSeenCount, x, y, z, facing, distanceSquared));
			}
		}
	}

	private void startResultFlushTimer() {
		stopResultFlushTimer();
		this.resultFlushTimer = new Timer((int) RESULT_FLUSH_INTERVAL_MS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				flushPendingResultRows();
			}
		});
		this.resultFlushTimer.start();
	}

	private void stopResultFlushTimer() {
		if(this.resultFlushTimer != null) {
			this.resultFlushTimer.stop();
			this.resultFlushTimer = null;
		}
	}

	private void flushPendingResultRows() {
		if(this.resultsTableModel == null) return;
		List<ResultRow> rows;
		synchronized(this.pendingResultRowsLock) {
			if(this.pendingResultRows.isEmpty()) {
				updateResultsViewerStatus();
				return;
			}
			rows = new ArrayList<ResultRow>(this.pendingResultRows);
			this.pendingResultRows.clear();
		}

		for(ResultRow row : rows) {
			this.resultsTableModel.addRow(new Object[] {
				Integer.valueOf(row.index),
				Integer.valueOf(row.x),
				Integer.valueOf(row.y),
				Integer.valueOf(row.z),
				row.facing,
				Long.valueOf(row.distanceSquared)
			});
			this.displayedResultCount++;
		}
		updateResultsViewerStatus();
	}

	private void clearResultsViewer(boolean resetCounters) {
		if(this.resultsTableModel != null) {
			this.resultsTableModel.setRowCount(0);
		}
		synchronized(this.pendingResultRowsLock) {
			this.pendingResultRows.clear();
			if(resetCounters) {
				this.resultsSeenCount = 0;
				this.displayedResultCount = 0;
			}
		}
		updateResultsViewerStatus();
	}

	private void updateResultsViewerStatus() {
		if(this.lblResultsViewerStatus == null) return;
		int total;
		synchronized(this.pendingResultRowsLock) {
			total = this.resultsSeenCount;
		}
		if(total == 0) {
			this.lblResultsViewerStatus.setText("No results yet. Results are also written to the selected file.");
		} else if(total > RESULT_VIEWER_ROW_LIMIT) {
			this.lblResultsViewerStatus.setText("Showing first " + formatCompactNumber(RESULT_VIEWER_ROW_LIMIT) + " of " + formatCompactNumber(total) + " matches. Full list is in the results file.");
		} else {
			this.lblResultsViewerStatus.setText("Showing " + formatCompactNumber(this.displayedResultCount) + " of " + formatCompactNumber(total) + " matches. Full list is also saved.");
		}
	}

	private void copySelectedTeleportCommand() {
		if(this.resultsTable == null || this.resultsTable.getSelectedRow() < 0) {
			showInfo("No result selected", "Select a row in the results viewer first.");
			return;
		}
		int modelRow = this.resultsTable.convertRowIndexToModel(this.resultsTable.getSelectedRow());
		String command = "/tp "
			+ this.resultsTableModel.getValueAt(modelRow, 1) + " "
			+ this.resultsTableModel.getValueAt(modelRow, 2) + " "
			+ this.resultsTableModel.getValueAt(modelRow, 3);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(command), null);
		if(this.lblResultsViewerStatus != null) {
			this.lblResultsViewerStatus.setText("Copied " + command + " to clipboard.");
		}
	}


	private void updateSearchEstimateLabel() {
		if(this.lblSearchEstimate == null) return;

		SearchEstimate estimate = estimateCurrentSearch();
		this.lblSearchEstimate.setText("Estimated " + formatEstimatedMatches(estimate.expectedMatches)
			+ " matches for this pattern/radius");
	}

	private SearchEstimate estimateCurrentSearch() {
		double candidateViews = Math.max(0.0d, estimateCandidateViews());
		double entropyBits = 0.0d;

		for(int x = 0; x < PATTERN_SIZE; ++x) {
			for(int y = 0; y < PATTERN_SIZE; ++y) {
				for(int z = 0; z < PATTERN_SIZE; ++z) {
					Vector3 pos = new Vector3(x, y, z);
					if(this.pattern.getValue(pos) == 4) continue;

					EnumBlockType blockType = EnumBlockType.fromId(this.blockTypes.getValue(pos));
					int stateCount = Math.max(1, blockType.getGuiStateCount());
					entropyBits += Math.log(stateCount) / Math.log(2.0d);
				}
			}
		}

		double expectedMatches;
		if(candidateViews <= 0.0d) {
			expectedMatches = 0.0d;
		} else {
			expectedMatches = candidateViews / Math.pow(2.0d, entropyBits);
		}

		if(Double.isNaN(expectedMatches) || expectedMatches < 0.0d) {
			expectedMatches = 0.0d;
		}

		return new SearchEstimate(candidateViews, expectedMatches, entropyBits);
	}

	private double estimateCandidateViews() {
		double side = (2.0d * Math.max(0, this.radius)) + 1.0d;
		double yRange = Math.max(0, this.yMax - this.yMin);
		double facings = this.rotation.getViewCount();
		return side * side * yRange * facings;
	}

	private long estimateCoordinateCandidates() {
		double side = (2.0d * Math.max(0, this.radius)) + 1.0d;
		double yRange = Math.max(0, this.yMax - this.yMin);
		double candidates = side * side * yRange;
		if(candidates >= Long.MAX_VALUE) return Long.MAX_VALUE;
		return Math.max(0L, (long) candidates);
	}

	private String formatEstimatedMatches(double value) {
		if(Double.isNaN(value)) return "0";
		if(Double.isInfinite(value)) return "too many";
		if(value <= 0.0d) return "0";
		if(value > 0.0d && value < 0.01d) return "<0.01";
		return formatCompactNumber(value);
	}

	private String formatRate(double candidatesPerSecond) {
		if(candidatesPerSecond <= 0.0d || Double.isNaN(candidatesPerSecond) || Double.isInfinite(candidatesPerSecond)) {
			return "—";
		}
		return formatCompactNumber(candidatesPerSecond) + " candidates/sec";
	}

	private String formatEta(long seconds) {
		if(seconds < 0L) return "calculating...";
		if(seconds == 0L) return "done";
		long hours = seconds / 3600L;
		long minutes = (seconds % 3600L) / 60L;
		long secs = seconds % 60L;
		if(hours > 0L) return hours + "h " + minutes + "m " + secs + "s";
		if(minutes > 0L) return minutes + "m " + secs + "s";
		return secs + "s";
	}

	private String formatCompactNumber(long value) {
		return formatCompactNumber((double) value);
	}

	private String formatCompactNumber(double value) {
		double abs = Math.abs(value);
		if(abs >= 1_000_000_000_000.0d) return ONE_DECIMAL_FORMAT.format(value / 1_000_000_000_000.0d) + "T";
		if(abs >= 1_000_000_000.0d) return ONE_DECIMAL_FORMAT.format(value / 1_000_000_000.0d) + "B";
		if(abs >= 1_000_000.0d) return ONE_DECIMAL_FORMAT.format(value / 1_000_000.0d) + "M";
		if(abs >= 10_000.0d) return ONE_DECIMAL_FORMAT.format(value / 1_000.0d) + "K";
		if(abs >= 100.0d) return String.valueOf(Math.round(value));
		if(abs >= 10.0d) return ONE_DECIMAL_FORMAT.format(value);
		if(abs == Math.rint(abs)) return String.valueOf((long) value);
		return ONE_DECIMAL_FORMAT.format(value);
	}

	public EnumAccelerationMode getAccelerationMode() {
		return this.accelerationMode;
	}

	public void showErrorAsync(final String title, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				showError(title, message);
			}
		});
	}

	public int getThreadCount() {
		return this.threadCount;
	}

	public void initializeSaveFile() {
		String requested = this.saveResultsField == null ? "" : this.saveResultsField.getText().trim();
		if(!requested.isEmpty()) {
			this.saveResultsPath = normalizeTxtPath(requested);
		} else {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
			LocalDateTime now = LocalDateTime.now();
			this.saveResultsPath = "coordinates_" + dtf.format(now) + ".txt";
		}
	}

	private String normalizeTxtPath(String path) {
		return path.endsWith(".txt") ? path : path + ".txt";
	}

	public String getSaveFile() {
		return this.saveResultsPath;
	}

	private void showError(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	private static final class SearchEstimate {
		final double candidateViews;
		final double expectedMatches;
		final double entropyBits;

		SearchEstimate(double candidateViews, double expectedMatches, double entropyBits) {
			this.candidateViews = candidateViews;
			this.expectedMatches = expectedMatches;
			this.entropyBits = entropyBits;
		}
	}

	private static final class ResultRow {
		final int index;
		final int x;
		final int y;
		final int z;
		final String facing;
		final long distanceSquared;

		ResultRow(int index, int x, int y, int z, String facing, long distanceSquared) {
			this.index = index;
			this.x = x;
			this.y = y;
			this.z = z;
			this.facing = facing;
			this.distanceSquared = distanceSquared;
		}
	}
}
