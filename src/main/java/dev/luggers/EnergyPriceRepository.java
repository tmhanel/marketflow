package dev.luggers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class EnergyPriceRepository {
	private final Path pricePath;
	private final List<String> lines;

	public EnergyPriceRepository() {
		try {
			pricePath = Paths.get(Objects.requireNonNull(getClass().getResource("energyPrice.csv")).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		try {
			lines = Files.readAllLines(pricePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double getPrice(double fullTime) {
		int fullHours = (int) Math.floor(fullTime / 3600);
		double price;
		if (lines.size() <= fullHours) {
			fullHours = fullHours % lines.size();
		}
		String line = lines.get(fullHours);
		String[] values = line.split(";");
		price = Double.parseDouble(values[2].replace(",", "."));
		return price;
	}
}