package org.emau.icmvc.ttp.gics.emergency;
public enum GICSEProperties {

	Host("dbHost"), Port("dbPort"), Name("dbName"), User("dbUser"), Pass("dbPass"), Study("study"), OutputPath("output_path"), Date("date");

	private final String name;

	private GICSEProperties(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
