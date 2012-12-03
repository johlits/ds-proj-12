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
		browser.setJavascriptEnabled(true);
		browser.setVisible(true);
		
	}
	
	public void setText (final String txt, final String title) {
		if (isAlive())
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!shell.isDisposed())
						shell.setText(title);
					if (!browser.isDisposed())
						browser.setText(txt);
				}});
	}
	
	public void update (final String txt, final String title) {
		if (isAlive())
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					String s = String.format(
							"var n = new DOMParser().parseFromString('%s', 'text/xml').documentElement.firstChild;"+
							"var o = document.getElementById('main');"+
							"o.parentNode.replaceChild(document.importNode(n, true), o);",
							txt.replace("\n", "").replace("'", "\'"));
					if (!shell.isDisposed())
						shell.setText(title);
					if (!browser.isDisposed())
						browser.execute(s);
				}});
	}
	
	public void setTitle (final String title) {
		if (isAlive())
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!shell.isDisposed())
						shell.setText(title);
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
