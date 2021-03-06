package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRiskData;
import eu.riscoss.shared.JMissingData;

public class MissingDataDialog {
	
	DialogBox dialog = new DialogBox();
	
	MultiLayerInputForm inputForm = new MultiLayerInputMatrix();

	private String 	ras;
	RASPanel 		panel;
	
	public MissingDataDialog( JMissingData md, String ras ) {
		
		this.ras = ras;
		
		dialog.setText( "Enter Missing Data Information" );
		
		inputForm.load( md );
		
		DockPanel dock = new DockPanel();
		dock.add( inputForm, DockPanel.CENTER );
		dock.add( new Button( "Done", new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onDone();
			}
		} ), DockPanel.SOUTH );
		dialog.setWidget( dock );
		
	}
	
	public MissingDataDialog(RASPanel panel, JMissingData md, String ras ) {
			
			this.panel = panel;
			this.ras = ras;
			
			dialog.setText( "Enter Missing Data Information" );
			
			inputForm.load( md );
			
			DockPanel dock = new DockPanel();
			dock.add( inputForm, DockPanel.CENTER );
			dock.add( new Button( "Done", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onDone();
				}
			} ), DockPanel.SOUTH );
			dialog.setWidget( dock );
			
		}

	protected void onDone() {
		
		CodecRiskData crd = GWT.create( CodecRiskData.class );
		//String values = crd.encode( inputForm.getValueMap() ).toString();
		JSONValue values = crd.encode( inputForm.getValueMap() );
		
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/" + ras + "/missing-data" ).put().header( "values", values ).send( 
					
		RiscossJsonClient.setAnalysisMissingData(ras, values, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				
				RiscossJsonClient.updateSessionData(ras, new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						//Window.alert( "Done" );
						if (panel != null) {
							panel.reloadPanel();
						}
					}
					
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
				
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
		
		
		dialog.hide();
	}

	public void show() {
		dialog.show();
	}
	
}
