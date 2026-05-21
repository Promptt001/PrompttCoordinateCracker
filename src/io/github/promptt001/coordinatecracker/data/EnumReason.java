package io.github.promptt001.coordinatecracker.data;

public enum EnumReason {

	NONE(""),
	COUNT("Missing required argument(s)"),
	SYNTAX("Syntax error"),
	UNKNOWN("Unknown argument(s)");
	
	public final String description;
	
	private EnumReason(String description) {
		
		this.description = description;
		
	}
	
}
