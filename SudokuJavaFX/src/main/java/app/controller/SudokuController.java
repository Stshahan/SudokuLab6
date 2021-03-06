package app.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import app.Game;
import app.helper.SudokuCell;
import app.helper.SudokuStyler;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pkgEnum.eGameDifficulty;
import pkgEnum.ePuzzleViolation;
import pkgGame.Cell;
import pkgGame.Sudoku;
import pkgHelper.PuzzleViolation;

public class SudokuController implements Initializable {
//Let user fill each square, even if wrong, and keep track of mistakes. Add trashcan or delete onKeyPressedEvent.
	//if the finished puzzle isn't a sudoku, tell user that they failed.
	private Game game;

	@FXML
	private GridPane gpTop;

	@FXML
	private HBox hboxGrid;

	@FXML
	private HBox hboxNumbers;

	private int iCellSize = 45;
	private static final DataFormat myFormat = new DataFormat("com.cisc181.Data.Cell");
	private static final DataFormat myTrashFormat = new DataFormat("com.cisc181.trashcan");
	private eGameDifficulty eGD = null;
	private Sudoku s = null;

	public void setMainApp(Game game) {
		this.game = game;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	/**
	 * btnStartGame - Fire this event when the 'start game' button is pushed
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	@FXML
	private void btnStartGame(ActionEvent event) {
		CreateSudokuInstance();
		BuildGrids();
	}

	/**
	 * CreateSudokuInstance - Create an instance of Sudoku, set the attribute in the 'Game' class
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	private void CreateSudokuInstance() {
		eGD = this.game.GetGameDifficulty();
		s = game.StartSudoku(this.game.GetGameSize(), eGD);
	}

	/**
	 * BuildGrid - This method will bild all the grid objects (top/sudoku/numbers)
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	private void BuildGrids() {

		// Paint the top grid(the place where the actual puzzle is)on the form
		BuildTopGrid();
		GridPane gridSudoku = BuildSudokuGrid();

		// gridSudoku.getStyleClass().add("GridPane");

		// Clear the hboxGrid, add the Sudoku puzzle
		hboxGrid.getChildren().clear(); // Clear any controls in the VBox
		// hboxGrid.getStyleClass().add("VBoxGameGrid");
		hboxGrid.getChildren().add(gridSudoku);

		// Clear the hboxNumbers, add the numbers
		GridPane gridNumbers = BuildNumbersGrid();
		//This is the portion of the stage that has the draggable numbers for the player to use.
		hboxNumbers.getChildren().clear();
		hboxNumbers.setPadding((new Insets(25, 25, 25, 25)));
		hboxNumbers.getChildren().add(gridNumbers);

	}

	/**
	 * BuildTopGrid - This is the grid at the top of the scene.  I'd stash 'difficulty', {@link #btnStartGame(ActionEvent)}of mistakes, etc
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	private void BuildTopGrid() {
		//Top hint grid builder
		gpTop.getChildren().clear();

		Label lblDifficulty = new Label(eGD.toString());
		gpTop.add(lblDifficulty, 0, 0);
		
		Label lblMistakes = new Label(this.game.getSudoku().getMistakesMessage());
		gpTop.add(lblMistakes, 1, 0);		

		ColumnConstraints colCon = new ColumnConstraints();
		colCon.halignmentProperty().set(HPos.CENTER);
		gpTop.getColumnConstraints().add(colCon);

		RowConstraints rowCon = new RowConstraints();
		rowCon.valignmentProperty().set(VPos.CENTER);
		gpTop.getRowConstraints().add(rowCon);

		gpTop.getStyleClass().add("GridPaneInsets");
	}

	/**
	 * BuildNumbersGrid - This is the 'numbers' grid... a grid of the avaiable numbers based on the
	 * flavor of the game.  If you're playing 4x4, you'll get numbers 1, 2, 3, 4.
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	private GridPane BuildNumbersGrid() {
		//builds bottom numbers list.
		Sudoku s = this.game.getSudoku();
		SudokuStyler ss = new SudokuStyler(s);
		GridPane gridPaneNumbers = new GridPane();
		gridPaneNumbers.setCenterShape(true);
		gridPaneNumbers.setMaxWidth(iCellSize + 15);
		for (int iCol = 0; iCol < s.getiSize(); iCol++) {

			gridPaneNumbers.getColumnConstraints().add(SudokuStyler.getGenericColumnConstraint(iCellSize));
			SudokuCell paneSource = new SudokuCell(new Cell(0, iCol));

			ImageView iv = new ImageView(GetImage(iCol + 1));
			paneSource.getCell().setiCellValue(iCol + 1);
			paneSource.getChildren().add(iv);

			paneSource.getStyleClass().clear(); // Clear any errant styling in the pane

			// Set a event handler to fire if the pane was clicked
			paneSource.setOnMouseClicked(e -> {
				System.out.println(paneSource.getCell().getiCellValue());
			});

			// This is going to fire if the number from the number grid is dragged
			// Find the cell in the pane, put it on the Dragboard
			
			//	Pay close attention... this is the method you must code to make your item draggable.
			//	If you want a paneTarget draggable (so you can drag it into the trash), you'll have to 
			//	implement a simliar method
			//paneSources are draggable
			//This whole event is what needs to happen to drag.
			paneSource.setOnDragDetected(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {

					/* allow any transfer mode */
					Dragboard db = paneSource.startDragAndDrop(TransferMode.ANY);

					/* put a string on dragboard */
					// Put the Cell on the clipboard, on the other side, cast as a cell
					ClipboardContent content = new ClipboardContent();
					content.put(myFormat, paneSource.getCell());
					db.setContent(content);
					event.consume();
				}
			});

			// Add the pane to the grid
			gridPaneNumbers.add(paneSource, iCol, 0);
		}
		
		//SudokuCell extends containerControl, StackPane. Put trash can in the container control.
		
		StackPane rmTrashCan = new StackPane();
		ImageView tc = new ImageView(this.GetPortalTrashCanImage());
		tc.setFitHeight(50);
		tc.setFitWidth(50);
		rmTrashCan.getChildren().add(tc);
		// 3 events to implement
		//each puzzle peice is a paneTarget
		//setOnDragDetected
		//
		
		rmTrashCan.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != rmTrashCan && event.getDragboard().hasContent(myTrashFormat)) {
					// Don't let the user drag over items that already have a cell value set
					
						//the line below allow for dropping
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						
				}
				event.consume();
			}
		});
		
		rmTrashCan.setOnDragDropped(new EventHandler<DragEvent>() 
		{//mistakes here
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				
		 
				if (db.hasContent(myTrashFormat)) {
					Cell CellFrom = (Cell) db.getContent(myTrashFormat);
					game.getSudoku().PrintPuzzle();
					event.setDropCompleted(success);
					event.consume();
					s.ChangeSudoku(CellFrom.getiRow(),  CellFrom.getiCol(), 0);
					BuildGrids();
					CellFrom.setiCellValue(0);
				}
			}
		});
		
		gridPaneNumbers.add(rmTrashCan,s.getiSize()+1,0);
		return gridPaneNumbers;
	}

	/**
	 * BuildSudokuGrid - This is the main Sudoku grid.  It cheats and uses SudokuStyler class to figure out the border
	 *	widths.  There are also methods implemented for drag/drop.
	 *
	 *	Example:
	 *	paneTarget.setOnMouseClicked - fires if the user clicks a paneTarget cell
	 *	paneTarget.setOnDragOver - fires if the user drags over a paneTarget cell
	 *	paneTarget.setOnDragEntered - fires as the user enters the draggable cell
	 *	paneTarget.setOnDragExited - fires as the user exits the draggable cell
	 *	paneTarget.setOnDragDropped - fires after the user drops a draggable item onto the paneTarget
	 * 
	 * @version 1.5
	 * @since Lab #5
	 * @param event
	 */
	
	private GridPane BuildSudokuGrid() {
		//Builds the actual puzzle. 
		Sudoku s = this.game.getSudoku();

		SudokuStyler ss = new SudokuStyler(s);
		GridPane gridPaneSudoku = new GridPane();
		gridPaneSudoku.setCenterShape(true);

		gridPaneSudoku.setMaxWidth(iCellSize * s.getiSize());
		gridPaneSudoku.setMaxHeight(iCellSize * s.getiSize());

		for (int iCol = 0; iCol < s.getiSize(); iCol++) {
			gridPaneSudoku.getColumnConstraints().add(SudokuStyler.getGenericColumnConstraint(iCellSize));
			gridPaneSudoku.getRowConstraints().add(SudokuStyler.getGenericRowConstraint(iCellSize));

			for (int iRow = 0; iRow < s.getiSize(); iRow++) {

				// The image control is going to be added to a StackPane, which can be centered

				SudokuCell paneTarget = new SudokuCell(new Cell(iRow, iCol));

				if (s.getPuzzle()[iRow][iCol] != 0) {
					ImageView iv = new ImageView(GetImage(s.getPuzzle()[iRow][iCol]));
					paneTarget.getCell().setiCellValue(s.getPuzzle()[iRow][iCol]);
					paneTarget.getChildren().add(iv);
				}

				paneTarget.getStyleClass().clear(); // Clear any errant styling in the pane
				paneTarget.setStyle(ss.getStyle(new Cell(iRow, iCol))); // Set the styling.

				paneTarget.setOnMouseClicked(e -> {
					System.out.println(paneTarget.getCell().getiCellValue());
				});

				//Added by Ryan
				paneTarget.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {

						/* allow any transfer mode */
						Dragboard db = paneTarget.startDragAndDrop(TransferMode.ANY);

						/* put a string on dragboard */
						// Put the Cell on the clipboard, on the other side, cast as a cell
						ClipboardContent content = new ClipboardContent();
						content.put(myTrashFormat, paneTarget.getCell());
						db.setContent(content);
						event.consume();
					}
				});
				
				// Fire this method as something is being dragged over a cell
				// I'm checking the cell value... if it's not zero... don't let it be dropped
				// (show the circle-with-line-through)
				paneTarget.setOnDragOver(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						if (event.getGestureSource() != paneTarget && event.getDragboard().hasContent(myFormat)) {
							// Don't let the user drag over items that already have a cell value set
							if (paneTarget.getCell().getiCellValue() == 0) {
								event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
							}
						}
						event.consume();
					}
				});

				// Fire this method as something is entering the item being dragged
				paneTarget.setOnDragEntered(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* show to the user that it is an actual gesture target */
						if (event.getGestureSource() != paneTarget && event.getDragboard().hasContent(myFormat)) {
							Dragboard db = event.getDragboard();
							Cell CellFrom = (Cell) db.getContent(myFormat);
							Cell CellTo = (Cell) paneTarget.getCell();
							if (CellTo.getiCellValue() == 0) {
								if (!s.isValidValue(CellTo.getiRow(), CellTo.getiCol(), CellFrom.getiCellValue())) {
									if (game.getShowHints()) {
										paneTarget.getChildren().add(0, SudokuStyler.getRedPane());
									}
								}
							}
						}
						event.consume();
					}
				});

				paneTarget.setOnDragExited(new EventHandler<DragEvent>() 
				{
					//turns off the red in the pane when the mouse leaves the pane while holding the number.
					public void handle(DragEvent event) {
						SudokuStyler.RemoveGridStyling(gridPaneSudoku);
						ObservableList<Node> childs = paneTarget.getChildren();
						for (Object o : childs) {
							if (o instanceof Pane)
								paneTarget.getChildren().remove(o);
						}
						event.consume();
					}
				}); 

				paneTarget.setOnDragDropped(new EventHandler<DragEvent>() 
				{//mistakes here
					public void handle(DragEvent event) {
						Dragboard db = event.getDragboard();
						boolean success = false;
						Cell CellTo = (Cell) paneTarget.getCell();
						
						//This will keep track of the mistakes.
						//private Sudoku int mistakeCounter = 0;
						
						//insert some sort of loop to check mistakes as the game is being played.
						
						//TODO: This is where you'll find mistakes.  
						//		Keep track of mistakes... as an attribute of Sudoku... start the attribute
						//		at zero, and expose a AddMistake(int) method in Sudoku to add the mistake
						//		write a getter so you can the value
						//		Might even have a max mistake attribute in eGameDifficulty (easy has 2 mistakes, medium 4, etc)
						//		If the number of mistakes >= max mistakes, end the game
						if (db.hasContent(myFormat)) {
							Cell CellFrom = (Cell) db.getContent(myFormat);
							
							if (!s.isValidValue(CellTo.getiRow(), CellTo.getiCol(), CellFrom.getiCellValue())) {
								
								s.ChangeSudoku(CellTo.getiRow(),  CellTo.getiCol(), CellFrom.getiCellValue());
								
								//	Add a mistake
								game.getSudoku().AddMistake();
								BuildTopGrid();
								
								//TODO: Set the message for mistakes
								if (game.getShowHints()) {
									//insert code here
									
									/*this is the logic for turning a cell red. 
									  we just need the logic for get row, get column and get region
									once we have that we can paint the hints using a for loop*/
									//gridPaneSudoku.addRow(CellTo.getiRow(), SudokuStyler.getRedPane());
									
									/*if (s.hasDuplicates(s.getRegion(CellTo.getiCol(), CellTo.getiRow()))) {
										int [] copy = new int [s.getiSize()];
										int [] zeros = new int [s.getiSize()];
										copy=s.getRegion(CellTo.getiCol(), CellTo.getiRow());
										//for(int iCol = 0; iCol<s.getiSize();iCol++){
										s.SetRegion(s.getRegionNbr(CellTo.getiCol(), CellTo.getiRow()),zeros);
										BuildGrids();
										for (int k=0;k<s.getiSize();k++) {
											int r = s.getRegionNbr(CellTo.getiCol(), CellTo.getiRow());
											int iSqrtSize = (int) Math.sqrt(s.getiSize());
											for (int i = (r / iSqrtSize) * iSqrtSize; i < ((r / iSqrtSize) * iSqrtSize) + iSqrtSize; i++) {
												for (int j = (r % iSqrtSize) * iSqrtSize; j < ((r % iSqrtSize) * iSqrtSize) + iSqrtSize; j++) {
											ImageView iv = new ImageView(GetBadImage(copy[k]));
											paneTarget.getCell().setiCellValue(copy[k]);
											paneTarget.getChildren().clear();
											paneTarget.getChildren().add(iv);
											success = true;
											event.setDropCompleted(success);
											gridPaneSudoku.add(paneTarget, CellTo.getiCol(), CellTo.getiRow());
											event.consume();
												}
											}
										}
									}*/
									ImageView iv = new ImageView(GetBadImage(CellFrom.getiCellValue()));
									paneTarget.getCell().setiCellValue(CellFrom.getiCellValue());
									paneTarget.getChildren().clear();
									paneTarget.getChildren().add(iv);
									System.out.println(CellFrom.getiCellValue());
									success = true;
									event.setDropCompleted(success);
									event.consume();
									
					//Shaun's problem				//TODO: fix duplicate children problem. 
									for(int startPosition= CellTo.getiCellValue() - (CellTo.getiCellValue()%s.getiSize()); startPosition<(startPosition+s.getiSize());startPosition++) {
										CellTo.setiCellValue(startPosition);
										for(int iCol = 0; iCol<s.getiSize();iCol++){
											
										ImageView badiv = new ImageView(GetBadImage(s.getPuzzle()[CellTo.getiRow()][iCol]));
										
										paneTarget.getCell().setiCellValue(s.getPuzzle()[CellTo.getiRow()][iCol]);
										paneTarget.getChildren().clear();
										
										//Ryan's code
										if((Object)startPosition instanceof Pane ) {
											paneTarget.getChildren().remove(startPosition);
										} //I imagine this would erase the existing gridPanes before painting the red ones on it.
										//SudokuStyler.RemoveGridStyling(gridPaneSudoku);

										
										
										paneTarget.getChildren().add(badiv);
										success = true;
										event.setDropCompleted(success);
										gridPaneSudoku.add(paneTarget, iCol, CellTo.getiRow()); 
										event.consume();
										}
										}
										
									
									
								}						}	
							if (!game.getShowHints()||(game.getShowHints() && s.isValidValue(CellTo.getiRow(), CellTo.getiCol(), CellFrom.getiCellValue())) ) {
								//	This is the code that is actually taking the cell value from the drag-from 
								//	cell and dropping a new Image into the dragged-to cell
								s.ChangeSudoku(CellTo.getiRow(),  CellTo.getiCol(), CellFrom.getiCellValue());
								ImageView iv = new ImageView(GetImage(CellFrom.getiCellValue()));
								paneTarget.getCell().setiCellValue(CellFrom.getiCellValue());
								paneTarget.getChildren().clear();
								paneTarget.getChildren().add(iv);
								System.out.println(CellFrom.getiCellValue());
								
								game.getSudoku().getPuzzle()[CellFrom.getiRow()][CellTo.getiCol()] = CellFrom.getiCellValue();
								
								success = true;}
						}
						game.getSudoku().PrintPuzzle();
						
						event.setDropCompleted(success);
						event.consume();
					}
					
				});
				//paneTarget.getChildren().add(0, SudokuStyler.getRedPane());
				 // Add the pane to the grid
				
				gridPaneSudoku.add(paneTarget, iCol, iRow); 
				if (game.getSudoku().isPuzzleMaxMistakes())
				{
					//	Game is over...  Max Mistakes reached
				}
				
				
				
			}

		}

		return gridPaneSudoku;
	}

	
	private void EndGame()
	{
		//	Disable the hboxNumbers items so they can't be dragged
		//	Show message that the game is over
		//	Allow them to 'clear' cells / reset mistakes
	}
	private Image GetImage(int iValue) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("img/" + iValue + ".png");
		return new Image(is);
	}

	private Image GetPortalTrashCanImage() {
		InputStream is = getClass().getClassLoader().getResourceAsStream("img/trashcan.png");
		return new Image(is);
	}


private Image GetBadImage(int iValue) {
	InputStream is = getClass().getClassLoader().getResourceAsStream("badimg/" + iValue + ".png");
	return new Image(is);
}


		
		//, CellTo.getiCol(), CellFrom.getiCellValue())) {
		

}