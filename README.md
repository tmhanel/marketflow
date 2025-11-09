# Hydropower Simulation 

This is a project of HCG member L.Z. It is based on the Isar Rivers Waterflow and Germanys Electricity Price Data.


## Build with Maven

Use

```bash
mvn verify site
```

to compile, run tests, measure test coverage and create nifty reports. See `target/site/index.html` or
run `mvn site:run` and open <http://localhost:8080/>.

## Run with Maven

Use

```bash
mvn javafx:run
```

for no-fuzz starting the simulation.


## Executable Jar

For yet unknown reasons, a fat jar will not be sufficient. At least when trying to access the referenced images and stylesheets, the FXMLLoader will complain, as he cannot load the files using the resulting URL (which gets mapped into the file system, not inside the jar).

```bash
java --enable-native-access=javafx.graphics  --module-path=/tmp/javafx-sdk-25.0.1/lib/ --add-modules="javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media"  -jar target/marketflow-1.0.0-SNAPSHOT.jar
``` 