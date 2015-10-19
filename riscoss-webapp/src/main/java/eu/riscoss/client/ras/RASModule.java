package eu.riscoss.client.ras;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.riskanalysis.RASPanel;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JRASInfo;

public class RASModule implements EntryPoint {

	DockPanel					panel = new DockPanel();
	
	CellTable<JRASInfo>			table;
	ListDataProvider<JRASInfo>	dataProvider;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.ras.RASModule::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	@Override
	public void onModuleLoad() {
		exportJS();
		
		table = new CellTable<JRASInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<JRASInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(JRASInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRAS(\"" + object.getId() + "\")" ); };
		}, "Risk Analysis Session");
		Column<JRASInfo,String> c = new Column<JRASInfo,String>(new ButtonCell() ) {
			@Override
			public String getValue(JRASInfo object) {
				return "Delete";
			}};
			c.setFieldUpdater(new FieldUpdater<JRASInfo, String>() {
				@Override
				public void update(int index, JRASInfo object, String value) {
					deleteRAS( object );
				}
			});
		table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<JRASInfo>();
		dataProvider.addDataDisplay( table );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.add( table );
		tablePanel.add( pager );
		
		mainView.setStyleName("mainViewLayer");
		//mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("400px");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		page.setWidth("100%");
		
		Label title = new Label("Risk Analysis Sessions");
		title.setStyleName("title");
		
		panel.add( tablePanel, DockPanel.CENTER );
		panel.setStyleName("margin-left");
		page.add(title);
		leftPanel.add(panel);
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		
		//RootPanel.get().add( panel );
		RootPanel.get().add( page );
		
		loadRASList();
	}
	
	public void loadRASList() {
		
		dataProvider.getList().clear();
		
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/list")
//			.get().send( new JsonCallback() 
		
		RiscossJsonClient.listRiskAnalysisSessions("", "", new JsonCallback(){
			public void onSuccess(Method method, JSONValue response) {
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				if( response.isArray() != null ) {
					CodecRASInfo codec = GWT.create( CodecRASInfo.class );
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						dataProvider.getList().add( info );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
	}
	
	RASPanel rasPanel;
	
	public void setSelectedRAS( String ras ) {
		rasPanel = new RASPanel(null);
		rasPanel.loadRAS(ras);
		RiscossJsonClient.getSessionSummary(ras, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				rightPanel.clear();
				JsonRiskAnalysis json = new JsonRiskAnalysis( response );
				Label title = new Label(json.getName());
				title.setStyleName("subtitle");
				rightPanel.add(title);
				rightPanel.add(rasPanel);
			}
		});
	}
	
	protected void deleteRAS( JRASInfo info ) {
		//new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/" + info.getId() + "/delete" ).delete().send( new JsonCallbackWrapper<JRASInfo>( info ) 
			
		RiscossJsonClient.deleteRiskAnalysisSession(info.getId(), new JsonCallbackWrapper<JRASInfo>( info ){
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				dataProvider.getList().remove( getValue() );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

}
