package dev.luggers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class PowerplantFactory {

	private static PowerplantFactory instance;

	private Powerplant firstPlant;

	private PowerplantFactory() {
		try {
			Path path = Paths.get(Objects.requireNonNull(getClass().getResource("powerplants.csv")).toURI());
			List<String> lines = Files.readAllLines(path);

			if (lines.isEmpty()) {
				throw new RuntimeException("Powerplants CSV file is empty");
			}

			Powerplant previousPlant = null;

			for (int i = 0; i < lines.size(); i++) {
				final var metadata = PowerplantMetadata.getMetadata(lines.get(i));
				final var currentPlant = new Powerplant(metadata);

				if (firstPlant == null) {
					firstPlant = currentPlant;
				} else {
					previousPlant.setNext(currentPlant);
				}
				previousPlant = currentPlant;
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load powerplants from CSV", e);
		}
	}

	public static PowerplantFactory getInstance() {
		if (instance == null) {
			instance = new PowerplantFactory();
		}
		return instance;
	}

	public Powerplant getFirstPowerplant() {
		return firstPlant;
	}
}
