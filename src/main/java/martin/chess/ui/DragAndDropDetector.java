package martin.chess.ui;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class DragAndDropDetector {

	public interface DragAndDropHandler {
		void onDragStarted();
		void onDrag();
		void onDragEnded();
	}
	
	private boolean isDragging = false;
	private DragAndDropHandler handler;
	private double x;
	private double y;
	
	public DragAndDropDetector(Node node, DragAndDropHandler handler) {
	
		this.handler = handler;
		
		node.setOnMouseReleased(this::onMouseReleased);
		node.setOnMouseDragged(this::onMouseDragged);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	private void onMouseDragged(MouseEvent e) {
		//System.out.println("Mouse dragged at " + e.getX() + ", " + e.getY());
		
		x = e.getX();
		y = e.getY();
		
		if (!isDragging) {
			isDragging = true;
			handler.onDragStarted();
		} else {
			handler.onDrag();
		}
	}


	private void onMouseReleased(MouseEvent e) {
		System.out.println("Mouse release detected at " + e.getX() + ", " + e.getY());
		
		x = e.getX();
		y = e.getY();
		
		if (isDragging) {
			isDragging = false;
			handler.onDragEnded();
		}
	}
	
}
