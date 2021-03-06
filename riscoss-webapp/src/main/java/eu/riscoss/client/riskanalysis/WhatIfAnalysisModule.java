/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.client.riskanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.Callback;
import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.client.codec.CodecWhatIfData;
import eu.riscoss.client.riskconfs.ModelSelectionDialog;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.JWhatIfData;
import eu.riscoss.shared.JWhatIfData.JWhatIfItem;

public class WhatIfAnalysisModule implements EntryPoint {
	
	List<String> models;
	
	DockPanel	dock = new DockPanel();
	ListBox		combo = new ListBox();
//	ListBox		rcCombo = new ListBox();
	SimplePanel	contentPanel = new SimplePanel();
	
	VerticalPanel		page = new VerticalPanel();
	VerticalPanel		mainView = new VerticalPanel();
	
	boolean ready = false;
	
	
	Map<String,IndicatorWidget> indicatorWidgets = new HashMap<String,IndicatorWidget>();
//	Map<String,RiskWidget> riskWidgets = new HashMap<String,RiskWidget>();
	Map<String,IndicatorWidget> riskWidgets = new HashMap<String,IndicatorWidget>();
	Map<String,IndicatorWidget> riskSessionWidgetsGoals = new HashMap<String,IndicatorWidget>();
	Map<String,IndicatorWidget> riskSessionWidgetsRisks = new HashMap<String,IndicatorWidget>();
	JWhatIfData 				jWhatIfData;
	
	HorizontalPanel buttons = new HorizontalPanel();
	String sid;
	List<String> entityList = new ArrayList<>();
	
	public void onModuleLoad() {
		
		sid = Window.Location.getParameter( "id" );
		String sb = Window.Location.getParameter( "entities" );
		
		//RootPanel.get().add( dock );
		
		page.setWidth("100%");
		
		Label title = new Label("What-If Analysis");
		title.setStyleName("title");
		page.add(title);
		mainView.setStyleName("mainViewPage");
		
		if (sid == null) {
			Button selectModels = new Button("Select models...");
			selectModels.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					ModelSelectionDialog dialog = new ModelSelectionDialog();
					dialog.show( new Callback<List<String>>() {
						@Override
						public void onError( Throwable t ) {
							// Do nothing
						}
						@Override
						public void onDone( List<String> list ) {
							models = list;
							RiscossJsonClient.listChunks( list, new JsonCallback() {
								@Override
								public void onFailure( Method method, Throwable exception ) {
									Window.alert( exception.getMessage() );
								}
	
								@Override
								public void onSuccess( Method method, JSONValue response ) {
	//								try {
										loadModel( response );
	//								} catch( Exception ex ) {
	//									Window.alert( ex.getMessage() );
	//								}
								}} );
						}
					});
				}
			});
			selectModels.setStyleName("button");
			buttons.add(selectModels);
			mainView.add(buttons);
			RiscossJsonClient.listModels( new JsonCallback() {
				
				@Override
				public void onSuccess(Method method, JSONValue response) {
					
					if( response.isArray() != null ) {
						for( int i = 0; i < response.isArray().size(); i++ ) {
							JSONObject o = (JSONObject)response.isArray().get( i );
							combo.addItem( o.get( "name" ).isString().stringValue() );
						}
					}
				}
				
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			});
		} else {
			Button back = new Button("Back");
			back.setStyleName("deleteButton");
			back.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					back();
				}
			});
			buttons.add(back);
			mainView.add(buttons);
			List<String> entities = new ArrayList<>();
			String s[] = sb.split("@");
			for (int i = 0; i < s.length; ++i) {
				Log.println(s[i]);
				entities.add(s[i]);
			}
			entityList = entities;
			RiscossJsonClient.getSessionData(sid, entities, new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					// TODO Auto-generated method stub	
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					models = new ArrayList<>();
					CodecWhatIfData codec = GWT.create( CodecWhatIfData.class );
					jWhatIfData = codec.decode( response );
					for (String ent : jWhatIfData.items.keySet()) {
						if (entityList.contains(ent))
							for (String s : jWhatIfData.items.get(ent).models) {
								Log.println(s);
								models.add(s);
							}
					}
					RiscossJsonClient.listChunks(models, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							loadModel(response);	
						}
					});
				}
			});
		}
		
		mainView.add(contentPanel);
		
		page.add(mainView);
		
		RootPanel.get().add(page);
		
	}
	
	VerticalPanel right1;
	
	protected void loadModel( JSONValue response ) {
		
		HorizontalPanel h = new HorizontalPanel();
		h.setSize( "100%", "100%" );
		VerticalPanel left1 = new VerticalPanel();
		VerticalPanel left2 = new VerticalPanel();
		VerticalPanel left3 = new VerticalPanel();
		right1 = new VerticalPanel();
		resPanel = new VerticalPanel();
		
		JSONObject jo = response.isObject();
		
		if( jo == null ) return;
		
		if( jo.get( "inputs" ) != null ) {
			JSONArray inputs = jo.get( "inputs" ).isArray();
			for( int i = 0; i < inputs.size(); i++ ) {
				JSONObject jinput = inputs.get( i ).isObject();
				IndicatorWidget iw = new IndicatorWidget( jinput );
				if (i < inputs.size()/3) left1.add( iw );
				else if (i >= inputs.size()/3 && i < 2*inputs.size()/3) left2.add( iw );
				else left3.add( iw );
				iw.addListener( new IndicatorWidget.Listener() {
					@Override
					public void IndicatorValueChanged() {
						runAnalysis();
					}
				});
				indicatorWidgets.put( jinput.get( "id" ).isString().stringValue(), iw );
			}
		}
		
		Label whatIf = new Label("What-if results");
		Label riskSes = new Label("Risk session results");
		whatIf.setStyleName("columnTitle");
		whatIf.setWidth("100%");
		riskSes.setStyleName("columnTitle");
		riskSes.setWidth("100%");
		right1.add(whatIf);
		resPanel.add(riskSes);
		if( jo.get( "outputs" ) != null ) {
			JSONArray inputs = jo.get( "outputs" ).isArray();
			for( int i = 0; i < inputs.size(); i++ ) {
				JSONObject jinput = inputs.get( i ).isObject();
				IndicatorWidget rw = new IndicatorWidget( jinput );
//				RiskWidget rw = new RiskWidget( jinput );
//				rw.setValue( "0", "0" );
				right1.add( rw );
				riskWidgets.put( jinput.get( "label" ).isString().stringValue(), rw );
			}
		}
		
		if (sid != null) {
			for (JWhatIfItem item : jWhatIfData.items.values()) {
				for (String id : item.values.keySet()) {
					if (indicatorWidgets.containsKey(id)) {
						String s = item.values.get(id);
						JSONObject json = JSONParser.parseLenient(s).isObject();
						String value = json.get("value").isString().stringValue();
						indicatorWidgets.get(id).setValue(value);
					}
				}
			}
			generateRiskSessionResults();
		}
		
		left1.setSpacing(10);
		left2.setSpacing(10);
		left3.setSpacing(10);
		h.add( left1 );
		h.add( left2 );
		h.add( left3 );
		h.add( right1 );
		if (sid != null) h.add( resPanel );
		right1.setSpacing(20);
		right1.setStyleName("rightPanelLayer");
		resPanel.setSpacing(20);
		resPanel.setStyleName("rightPanelLayer");
		
		contentPanel.setWidget( h );
		ready = true;
		runAnalysis();
	}

	VerticalPanel resPanel;
	
	protected void generateRiskSessionResults() {
		RiscossJsonClient.getSessionResults(sid, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JSONArray results = response.isObject().get("hresults").isObject().get("results").isArray();
				for (int i = 0; i < results.size(); ++i) {
					JSONObject jinput = results.get( i ).isObject();
					IndicatorWidget rw = new IndicatorWidget( jinput );
					Double d = jinput.get("e").isObject().get("e").isNumber().doubleValue();
					rw.setValue(String.valueOf(d));
//					RiskWidget rw = new RiskWidget( jinput );
//					rw.setValue( "0", "0" );
					if (jinput.get("type").isString().stringValue().equals("Goal")) riskSessionWidgetsGoals.put( jinput.get( "label" ).isString().stringValue(), rw );
					else riskSessionWidgetsRisks.put( jinput.get( "label" ).isString().stringValue(), rw );
				}
				for (IndicatorWidget rw : riskSessionWidgetsGoals.values()) {
					resPanel.add(rw);
				}
				for (IndicatorWidget rw : riskSessionWidgetsRisks.values()) {
					resPanel.add(rw);
				}
				sortSimulation();
			}
		});
	}
	
	protected void sortSimulation() {
		right1.clear();
		Label whatIf = new Label("What-if results");
		whatIf.setStyleName("columnTitle");
		whatIf.setWidth("100%");
		right1.add(whatIf);
		
		for (String s : riskSessionWidgetsGoals.keySet()) {
			right1.add(riskWidgets.get(s));
		}
		for (String s : riskSessionWidgetsRisks.keySet()) {
			right1.add(riskWidgets.get(s));
		}
	}
	
	Label running = new Label("Running...");
	
	protected void runAnalysis() {
		buttons.add(running);
		
		if( !ready ) {
			buttons.remove(running);
			return;
		}
		
		ready = false;
		
		JSONObject values = readIndicators();
		
		RiscossJsonClient.runWhatIfAnalysis( models, values, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
				ready = true;
				
			}
			@Override
			public void onSuccess( Method method, JSONValue object ) {
				ready = true;
				if( object.isObject() == null ) return;
				JSONArray response = object.isObject().get( "results" ).isArray();
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject v = response.isArray().get( i ).isObject();
					IndicatorWidget rw = riskWidgets.get( v.get( "id" ).isString().stringValue() );
					if( rw == null ) continue;
					if( "evidence".equals( v.get( "datatype" ).isString().stringValue() ) ) {
						rw.setValue( v );
//						rw.setValue( v.get( "p" ).isString().stringValue(), v.get( "m" ).isString().stringValue() );
					}
					else if( "distribution".equals( v.get( "datatype" ).isString().stringValue() ) ) {
						rw.setValue( v );
					}
				}
				buttons.remove(running);
			}} );
	}
	
	JSONObject readIndicators() {
		
		JSONObject o = new JSONObject();
		
		for( String id : indicatorWidgets.keySet() ) {
			IndicatorWidget iw = indicatorWidgets.get( id );
			if( id == null ) continue;
			o.put( id, iw.getJson() );
		}
		
		return o;
	}
	
	private void back() {
		Window.Location.replace("riskanalysis.jsp?id=" + sid);
	}
}
