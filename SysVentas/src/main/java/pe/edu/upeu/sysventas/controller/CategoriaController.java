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
import pe.edu.upeu.sysventas.model.Categoria;
import pe.edu.upeu.sysventas.service.ICategoriaService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Controller
public class CategoriaController {

    @FXML
    TextField txtNombreCategoria, txtFiltroDato;

    @FXML
    private TableView<Categoria> tableView;

    @FXML
    Label lbnMsg;

    @FXML
    private AnchorPane miContenedor;

    Stage stage;

    @Autowired
    ICategoriaService categoriaService;

    private Validator validator;
    ObservableList<Categoria> listarCategoria;
    Categoria formulario;
    Long idCategoriaCE = 0L;

    private void filtrarCategorias(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableView.getItems().clear();
            tableView.getItems().addAll(listarCategoria);
        } else {
            String lowerCaseFilter = filtro.toLowerCase();
            List<Categoria> categoriasFiltradas = listarCategoria.stream()
                    .filter(categoria -> categoria.getNombre().toLowerCase().contains(lowerCaseFilter))
                    .collect(Collectors.toList());
            tableView.getItems().clear();
            tableView.getItems().addAll(categoriasFiltradas);
        }
    }

    public void listar() {
        try {
            tableView.getItems().clear();
            listarCategoria = FXCollections.observableArrayList(categoriaService.findAll());
            tableView.getItems().addAll(listarCategoria);
            txtFiltroDato.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrarCategorias(newValue);
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

        TableViewHelper<Categoria> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Categoría", new ColumnInfo("idCategoria", 120.0));
        columns.put("Nombre Categoría", new ColumnInfo("nombre", 400.0));

        Consumer<Categoria> updateAction = (Categoria categoria) -> {
            System.out.println("Actualizar: " + categoria);
            editForm(categoria);
        };

        Consumer<Categoria> deleteAction = (Categoria categoria) -> {
            categoriaService.delete(categoria);
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
        txtNombreCategoria.getStyleClass().remove("text-field-error");
    }

    @FXML
    public void clearForm() {
        txtNombreCategoria.clear();
        idCategoriaCE = 0L;
        limpiarError();
        lbnMsg.setText("");
    }

    public void editForm(Categoria categoria) {
        txtNombreCategoria.setText(categoria.getNombre());
        idCategoriaCE = categoria.getIdCategoria();
        limpiarError();
    }

    private void mostrarErroresValidacion(Set<ConstraintViolation<Categoria>> violaciones) {
        limpiarError();

        if (!violaciones.isEmpty()) {
            ConstraintViolation<Categoria> primeraViolacion = violaciones.iterator().next();
            lbnMsg.setText(primeraViolacion.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

            if (primeraViolacion.getPropertyPath().toString().equals("nombre")) {
                txtNombreCategoria.getStyleClass().add("text-field-error");
                Platform.runLater(txtNombreCategoria::requestFocus);
            }
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        limpiarError();
        double width = stage.getWidth() / 1.5;
        double height = stage.getHeight() / 2;

        if (idCategoriaCE > 0L) {
            formulario.setIdCategoria(idCategoriaCE);
            categoriaService.update(idCategoriaCE, formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, width, height);
        } else {
            categoriaService.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, width, height);
        }
        clearForm();
        listar();
    }

    @FXML
    public void validarFormulario() {
        formulario = Categoria.builder()
                .nombre(txtNombreCategoria.getText())
                .build();

        Set<ConstraintViolation<Categoria>> violaciones = validator.validate(formulario);

        if (violaciones.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violaciones);
        }
    }
}
