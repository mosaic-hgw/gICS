package org.emau.icmvc.ttp.gics.emergency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("+++++++ gICS emergency program ++++++++");
		logger.info("Sollte das gICS einmal nicht lauffähig sein, kann dieses Programm für die Extraktion der ICs im PDF Format genutzt werden");
		logger.info("Die erzeugten Dateien befinden sich unter dem Pfad, der in der Properties-Datei angegeben wurde");

		if (args.length == 1) {

			try {
				DatabaseDecode dbd = new DatabaseDecode(args[0]);
				dbd.base64ToPDF();
				logger.info("Dateien erfolgreich exportiert");
			} catch (MissingPropertyException e) {
				logger.error("fehlende Werte in der Propertiesdatei", e);
			} catch (ClassNotFoundException e) {
				logger.error("Datenbanktreiber fehlt", e);
			} catch (SQLException e) {
				logger.error("Datenbankfehler beim Lesen der Scans", e);
			} catch (FileNotFoundException e) {
				logger.error("Properties-Datei (" + args[0] + ") wurde nicht gefunden", e);
			} catch (IOException e) {
				logger.error("Properties-Datei (" + args[0] + ") konnte nicht gelesen werden", e);
			}

		} else {
			System.err.println("Eingabeparameter erforderlich: database.properties");
		}
	}
}
