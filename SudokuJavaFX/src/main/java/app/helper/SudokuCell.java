package app.helper;

import java.io.Serializable;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import pkgGame.Cell;

public class SudokuCell extends StackPane implements Serializable {

	private Cell cell;
//gives row, column, value.
	public SudokuCell(Cell c) {
		this.cell = c;
	}

	public Cell getCell() {
		return cell;
	}

}
