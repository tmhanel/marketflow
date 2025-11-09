package dev.luggers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class InflowRepository {
	protected DoubleProperty inflow = new SimpleDoubleProperty();
	private Path inflowPath;
	private List<String> lines;

	public InflowRepository() {
		try {
			inflowPath = Paths.get((Objects.requireNonNull(getClass().getResource("inflow.csv"))).toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		try {
			lines = Files.readAllLines(inflowPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double getInflow(double fullTime) {
		int fullHours = (int) Math.floor(fullTime / 3600);
		int index = (fullHours * 4) % lines.size();

		String line = lines.get(index).trim();
		if (line.isEmpty()) {
			return getInflow(fullTime + 3600);
		}

		String[] values = line.split(";");
		String numeric = values[1];
		if (numeric.isEmpty()) {
			return getInflow(fullTime + 3600);
		}

		inflow.set(Double.parseDouble(values[1].replace(",", ".")));
		return inflow.get();
	}
}