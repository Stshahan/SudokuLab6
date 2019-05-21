package app.controller;

import java.net.URL;
import java.util.ResourceBundle;

import app.Game;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckMenuItem;
import javafx.event.ActionEvent;
import pkgEnum.eGameDifficulty;

public class GameBorderController implements Initializable {

	private Game game;
//sts
	@FXML
	private ToggleGroup tglSudokuSize;

	@FXML
	private ToggleGroup tglDifficulty;

	@FXML
	private CheckMenuItem chkShowHints;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void setMainApp(Game game) {
		this.game = game;
	}

	public int GetGameSize() {
		RadioMenuItem rmi = (RadioMenuItem) tglSudokuSize.getSelectedToggle();

		return Integer.parseInt(rmi.getId());
	}

	public eGameDifficulty GetGameDifficulty() {
		RadioMenuItem rmi = (RadioMenuItem) tglDifficulty.getSelectedToggle();
		eGameDifficulty eGD = null;

		switch (rmi.getText()) {
		case "Easy":
			eGD = eGameDifficulty.EASY;
			break;
		case "Medium":
			eGD = eGameDifficulty.MEDIUM;
			break;
		case "Hard":
			eGD = eGameDifficulty.HARD;
			break;
		}
		return eGD;
	}

	public boolean getShowHints()
	{
		return 	chkShowHints.isSelected();
	}
	
	@FXML
	private void mnuPuzzleSizeChange(ActionEvent event) {
	
		RadioMenuItem mi = (RadioMenuItem)event.getSource();
		System.out.println(mi.getId());
		switch (mi.getId())
		{
		case "4":
			this.game.setStageWidth(400);
			break;
		case "9":
			this.game.setStageWidth(600);
			break;

		}
		System.out.println("Puzzle Size Changed");
		
		
	}
}
