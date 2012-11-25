import org.eclipse.swt.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class GraphicalUserInterface {
	private Display display;
	private Shell shell;
	private Browser browser;
	
	public GraphicalUserInterface () {
		display = new Display();
		shell = new Shell(display);
		shell.setSize(1210, 710);
		shell.setText("Mighty traffic simulator");

		FillLayout fillLayout = new FillLayout();
		shell.setLayout(fillLayout);
		
		browser = new Browser(shell, SWT.NONE);
		browser.setJavascriptEnabled(false);
		browser.setVisible(true);
	}
	
	public void setURL (final String url, final String title) {
		if (isAlive())
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!shell.isDisposed())
						shell.setText(title);
					if (!browser.isDisposed())
						browser.setUrl(url);
				}});
	}
	
	public boolean isAlive () {
		return !display.isDisposed();
	}
	
	public void mainloop () {
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();
	}
}
