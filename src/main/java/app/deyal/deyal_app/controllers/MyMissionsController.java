package app.deyal.deyal_app.controllers;

import app.deyal.deyal_app.data.Mission;
import app.deyal.deyal_app.managers.DataManager;
import app.deyal.deyal_app.managers.StageManager;
import app.deyal.deyal_app.repository.MissionClient;
import app.deyal.deyal_app.repository.MissionEventClient;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Comparator;

public class MyMissionsController {
    //My Missions
    @FXML
    public TableView<Mission> myMissionTableView;
    @FXML
    public TableColumn<Mission, String> mmMissionTitleTableColumn;
    @FXML
    public TableColumn<Mission, String> mmMissionStatusTableColumn;
    @FXML
    public TableColumn<Mission, String> mmMissionLevelTableColumn;
    @FXML
    public TableColumn<Mission, String> mmMissionDescriptionTableColumn;

    @FXML
    public void initialize() {
        this.loadMyMissions();
    }

    public void loadMyMissions() {
        if (!MissionClient.getMyMissionList(DataManager.getInstance().getToken())) {    //show user's mission data retrieve failed
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Failed");
            alert.setHeaderText("My mission list retrieve Failed!");
            alert.setContentText("Please check your Internet connection.");
            alert.showAndWait();
        } else {
            ArrayList<Mission> missionArrayList = DataManager.getInstance().myMissionsList;

            mmMissionTitleTableColumn.setCellValueFactory(new PropertyValueFactory<Mission, String>("title"));
            mmMissionLevelTableColumn.setCellValueFactory(new PropertyValueFactory<Mission, String>("difficulty") {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Mission, String> param) {
                    Mission mission = param.getValue();
                    return new ReadOnlyObjectWrapper<>(mission.getDifficultyAsString());
                }
            });
            mmMissionDescriptionTableColumn.setCellValueFactory(new PropertyValueFactory<Mission, String>("description"));
            mmMissionStatusTableColumn.setCellValueFactory(new PropertyValueFactory<Mission, String>("id") {   //set status (completed, created, failed, ongoing) of my missions
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Mission, String> param) {
                    Mission mission = param.getValue();
                    String status = mission.findStatus(mission.getId());
                    return new ReadOnlyObjectWrapper<>(status);
                }
            });
            myMissionTableView.getItems().setAll(missionArrayList);

            // custom sort My Mission table
            myMissionTableView.setSortPolicy(tv -> {
                final ObservableList<Mission> itemsList = myMissionTableView.getItems();
                if (itemsList == null || itemsList.isEmpty()) {
                    return true;
                }
                final ArrayList<TableColumn<Mission, ?>> columns = new ArrayList<>(myMissionTableView.getSortOrder());
                if (columns.isEmpty()) {
                    return true;
                }
                FXCollections.sort(itemsList, (a, b) -> {
                    for (TableColumn<Mission, ?> col : columns) {
                        if (col.getSortType() == null || !col.isSortable()) {
                            continue;
                        }

                        Object value1 = col.getCellData(a);
                        Object value2 = col.getCellData(b);
                        if (mmMissionLevelTableColumn.equals(col)) {
                            value1 = a.getDifficulty();
                            value2 = b.getDifficulty();
                        }

                        @SuppressWarnings("unchecked") final Comparator<Object> c = (Comparator<Object>) col.getComparator();
                        final int result = TableColumn.SortType.ASCENDING.equals(col.getSortType()) ? c.compare(value1, value2)
                                : c.compare(value2, value1);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                });
                return true;
            });

            //selecting a mission from dashboard
            myMissionTableView.setOnMouseClicked((MouseEvent event) -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    int index = myMissionTableView.getSelectionModel().getSelectedIndex();
                    DataManager.getInstance().tempMission = myMissionTableView.getItems().get(index); //sometime shows index out of bound error
                    if (!MissionEventClient.getMissionEventList(DataManager.getInstance().token,
                            DataManager.getInstance().tempMission.getId())) {   //show mission event list retrieve failed
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Failed");
                        alert.setHeaderText("Mission event list retrieve Failed!");
                        alert.setContentText("Please check your Internet connection.");
                        alert.showAndWait();
                    }
                    StageManager.getInstance().createViewMissionStage();
                    StageManager.getInstance().viewMissionStage.showAndWait();
                }
            });
        }
    }

}
