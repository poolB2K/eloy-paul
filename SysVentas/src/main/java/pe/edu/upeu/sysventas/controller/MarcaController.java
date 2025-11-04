package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.model.Marca;
import pe.edu.upeu.sysventas.service.IMarcaService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Controller
public class MarcaController {

    @FXML
    TextField txtNombreMarca, txtFiltroDato;

    @FXML
    private TableView<Marca> tableView;

    @FXML
    Label lbnMsg;

    @FXML
    private AnchorPane miContenedor;

    Stage stage;

    @Autowired
    IMarcaService marcaService;

    private Validator validator;
    ObservableList<Marca> listarMarca;
    Marca formulario;
    Long idMarcaCE = 0L;

    private void filtrarMarcas(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableView.getItems().clear();
            tableView.getItems().addAll(listarMarca);
        } else {
            String lowerCaseFilter = filtro.toLowerCase();
            List<Marca> marcasFiltradas = listarMarca.stream()
                    .filter(marca -> marca.getNombre().toLowerCase().contains(lowerCaseFilter))
                    .collect(Collectors.toList());
            tableView.getItems().clear();
            tableView.getItems().addAll(marcasFiltradas);
        }
    }

    public void listar() {
        try {
            tableView.getItems().clear();
            listarMarca = FXCollections.observableArrayList(marcaService.findAll());
            tableView.getItems().addAll(listarMarca);
            txtFiltroDato.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrarMarcas(newValue);
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000),
                event -> {
                    stage = (Stage) miContenedor.getScene().getWindow();
                    if (stage != null) {
                        System.out.println("El título del stage es: " + stage.getTitle());
                    } else {
                        System.out.println("Stage aún no disponible.");
                    }
                }));
        timeline.setCycleCount(1);
        timeline.play();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        TableViewHelper<Marca> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Marca", new ColumnInfo("idMarca", 120.0));
        columns.put("Nombre Marca", new ColumnInfo("nombre", 400.0));

        Consumer<Marca> updateAction = (Marca marca) -> {
            System.out.println("Actualizar: " + marca);
            editForm(marca);
        };

        Consumer<Marca> deleteAction = (Marca marca) -> {
            marcaService.delete(marca);
            double with = stage.getWidth() / 1.5;
            double h = stage.getHeight() / 2;
            Toast.showToast(stage, "Se eliminó correctamente!!", 2000, with, h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        listar();
    }

    public void limpiarError() {
        txtNombreMarca.getStyleClass().remove("text-field-error");
    }

    @FXML
    public void clearForm() {
        txtNombreMarca.clear();
        idMarcaCE = 0L;
        limpiarError();
        lbnMsg.setText("");
    }

    public void editForm(Marca marca) {
        txtNombreMarca.setText(marca.getNombre());
        idMarcaCE = marca.getIdMarca();
        limpiarError();
    }

    private void mostrarErroresValidacion(Set<ConstraintViolation<Marca>> violaciones) {
        limpiarError();

        if (!violaciones.isEmpty()) {
            ConstraintViolation<Marca> primeraViolacion = violaciones.iterator().next();
            lbnMsg.setText(primeraViolacion.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

            if (primeraViolacion.getPropertyPath().toString().equals("nombre")) {
                txtNombreMarca.getStyleClass().add("text-field-error");
                Platform.runLater(txtNombreMarca::requestFocus);
            }
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        limpiarError();
        double width = stage.getWidth() / 1.5;
        double height = stage.getHeight() / 2;

        if (idMarcaCE > 0L) {
            formulario.setIdMarca(idMarcaCE);
            marcaService.update(idMarcaCE, formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, width, height);
        } else {
            marcaService.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, width, height);
        }
        clearForm();
        listar();
    }

    @FXML
    public void validarFormulario() {
        formulario = Marca.builder()
                .nombre(txtNombreMarca.getText())
                .build();

        Set<ConstraintViolation<Marca>> violaciones = validator.validate(formulario);

        if (violaciones.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violaciones);
        }
    }
}