/*
 * Copyright 2012-2013 Ontology Engineering Group, Universidad Politécnica de Madrid, Spain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package widoco.gui;

import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import widoco.Configuration;
import widoco.CreateDocInThread;
import widoco.CreateResources;
import widoco.TextConstants;
import widoco.entities.Agent;
import widoco.entities.License;
import widoco.entities.Ontology;



/**
 *
 * @author Daniel Garijo
 */
public class GuiController {

    
    public enum State{initial, metadata, sections, configLODE, loading, generated, exit};
    private State state;
    private JFrame gui;
    private Configuration config;
    private File tmpFile;

    public GuiController() {
        this.state = State.initial;  
        config = new Configuration();
        //read logo
        gui = new GuiStep1(this);
        gui.setVisible(true);
        try {
            //create a temporal folder with all LODE resources
            tmpFile = new File("tmp"+new Date().getTime());
            tmpFile.mkdir();
            CreateResources.copyResourceFolder(TextConstants.lodeResources, tmpFile.getName());
        } catch (IOException ex) {
            System.err.println("Error while creating the temporal file");
        }
        
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            
        }
        
    }

    public Configuration getConfig() {
        return config;
    }
    
    
    
    public HashMap<String,String> getEditableProperties(){
        HashMap<String,String> props = new HashMap<String,String>();
        if(config.getTitle()!=null)props.put("Ontology Title", config.getTitle());
        if(config.getMainOntology()!=null){
            props.put("Ontology Name", config.getMainOntology().getName());
            props.put("Ontology Prefix", config.getMainOntology().getNamespacePrefix());
            props.put("Ontology Namespace URI", config.getMainOntology().getNamespaceURI());
        }
        if(config.getReleaseDate()!=null)props.put("Date of Release", config.getReleaseDate());
        if(config.getThisVersion()!=null)props.put("This Version", config.getThisVersion());
        if(config.getLatestVersion()!=null)props.put("Latest Version", config.getLatestVersion());
        if(config.getPreviousVersion()!=null)props.put("Previous Version", config.getPreviousVersion());
        if(config.getRevision()!=null)props.put("Ontology Revision", config.getRevision());
        //authors go here in a loop
        if(config.getCreators()!=null){
            ArrayList<Agent> creators = config.getCreators();
            Iterator<Agent> it = creators.iterator();
            String author="",authorUri="",authorInsti="";
            while(it.hasNext()){
                Agent a = it.next();
                author +=a.getName()+";";
                if(a.getURL()!=null)authorUri+=a.getURL()+";";
                if(a.getInstitutionName()!=null)authorInsti += a.getInstitutionName()+";";
            }
            if(!"".equals(author)) props.put("Author", author.substring(0, author.length()-1));
            if(!"".equals(authorUri)) props.put("Author URI", authorUri.substring(0,authorUri.length()-1));
            if(!"".equals(authorInsti)) props.put("Author Institution", authorInsti.substring(0, authorInsti.length()-1));
        }
        if(config.getContributors()!=null){
            //contributors go here in a loop
            ArrayList<Agent> contributors = config.getContributors();
            Iterator<Agent> itContr = contributors.iterator();
            String contributor="",contributorUri="",contributorInsti="";
            while(itContr.hasNext()){
                Agent a = itContr.next();
                contributor +=a.getName()+";";
                if(a.getURL()!=null)contributorUri+=a.getURL()+";";
                if(a.getInstitutionName()!=null)contributorInsti += a.getInstitutionName()+";";
            }
            if(!"".equals(contributor))props.put("Contributor", contributor.substring(0,contributor.length()-1));
            if(!"".equals(contributorUri))props.put("Contributor URI", contributorUri.substring(0,contributorUri.length()-1));
            if(!"".equals(contributorInsti))props.put("Contributor Institution", contributorInsti.substring(0,contributorInsti.length()-1));
        }
        if(config.getImportedOntolgies()!=null){
            ArrayList<Ontology> imported = config.getImportedOntolgies();
            Iterator<Ontology> itImported = imported.iterator();
            String importedName="",importedUri="";
            while(itImported.hasNext()){
                Ontology a = itImported.next();
                importedName +=a.getName()+";";
                importedUri+=a.getNamespaceURI()+";";
            }
            if(!"".equals(importedName))props.put("Imported Ontologies Names", importedName.substring(0,importedName.length()-1));
            if(!"".equals(importedUri))props.put("Imported Ontologies URIs", importedUri.substring(0,importedUri.length()-1));
        }
        
        if(config.getExtendedOntologies()!=null){
            //extended ontos go here in a loop
            ArrayList<Ontology> extended = config.getExtendedOntologies();
            Iterator<Ontology> itExtended = extended.iterator();
            String extendedName="",extendedUri="";
            while(itExtended.hasNext()){
                Ontology a = itExtended.next();
                extendedName +=a.getName()+";";
                extendedUri+=a.getNamespaceURI()+";";
            }
            if(!"".equals(extendedName))props.put("Extended Ontologies Names", extendedName.substring(0,extendedName.length()-1));
            if(!"".equals(extendedUri))props.put("Extended Ontology URLS", extendedUri.substring(0,extendedUri.length()-1));
        }
        //license
        if(config.getLicense()!=null){
            props.put("License Name", config.getLicense().getName());
            props.put("License URI", config.getLicense().getUrl());
            if(config.getLicense().getIcon()!=null){
                props.put("License icon URL", config.getLicense().getIcon());
            }
        }
        return props;
    }
    
    public void reloadConfiguration(String path){
        this.config.reloadPropertyFile(path);
    }
    
    public void saveEditableProperties(HashMap properties){
        //config.set
        //we don't check wether it exists because the property mght have been deleted.
    
        config.setTitle((String)properties.get("Ontology Title"));
        //mandatory
        if(properties.containsKey("Ontology Name")){
            config.getMainOntology().setName((String)properties.get("Ontology Name"));
        }
        if(properties.containsKey("Ontology Prefix")){
            config.getMainOntology().setNamespacePrefix((String)properties.get("Ontology Prefix"));
        }
        if(properties.containsKey("Ontology Namespace URI")){
            config.setOntologyURI((String)properties.get("Ontology Namespace URI"));
        }
        config.setReleaseDate((String)properties.get("Date of Release"));
        config.setThisVersion((String)properties.get("This Version"));
        config.setLatestVersion((String)properties.get("Latest Version"));
        config.setPreviousVersion((String)properties.get("Previous Version"));
        config.setRevision((String)properties.get("Ontology Revision"));
        config.getLicense().setName((String)properties.get("License Name"));
        config.getLicense().setUrl((String)properties.get("License URI"));        
        config.getLicense().setIcon((String)properties.get("License icon URL"));  
        
        if(properties.containsKey("Author")){
            String authorValue = (String)properties.get("Author");
            if(!"".equals(authorValue)){
                String[] authors = authorValue.split(";");
                String[] authorURIs = null;
                String[] authorInstis = null;
                ArrayList<Agent> newAuthors = new ArrayList<Agent>();
                if(properties.containsKey("Author URL")){
                    if(!"".equals(properties.get("Author URL"))){
                        authorURIs = ((String)properties.get("Author URL")).split(";");
                    }
                }
                if(properties.containsKey("Author Institution")){
                    if(!"".equals(properties.get("Author Institution"))){
                        authorInstis = ((String)properties.get("Author Institution")).split(";");
                    }
                }
                for(int i=0; i< authors.length;i++){
                    Agent author = new Agent();
                    author.setName(authors[i]);
                    if (authorURIs!=null && authors.length == authorURIs.length ){
                        author.setURL(authorURIs[i]);
                    }
                    if (authorInstis!=null && authors.length == authorInstis.length ){
                        author.setInstitutionName(authorInstis[i]);
                    }
                    newAuthors.add(author);
                }
                config.setCreators(newAuthors);
            }
        }
        
        if(properties.containsKey("Contributor")){
            String contribValue = (String)properties.get("Contributor");
            if(!"".equals(contribValue)){
                String[] contrib = contribValue.split(";");
                String[] contribURI = null;
                String[] contribInsti = null;
                ArrayList<Agent> newContrib = new ArrayList<Agent>();
                if(properties.containsKey("Contributor URL")){
                    if(!"".equals(properties.get("Contributor URL"))){
                        contribURI = ((String)properties.get("Contributor URL")).split(";");
                    }
                }
                if(properties.containsKey("Contributor Institution")){
                    if(!"".equals(properties.get("Contributor Institution"))){
                        contribInsti = ((String)properties.get("Contributor Institution")).split(";");
                    }
                }
                
                for(int i=0; i< contrib.length;i++){
                    Agent contributor = new Agent();
                    contributor.setName(contrib[i]);
                    if (contribURI!=null && contrib.length == contribURI.length ){
                        contributor.setURL(contribURI[i]);
                    }
                    if (contribInsti!=null && contrib.length == contribInsti.length ){
                        contributor.setInstitutionName(contribInsti[i]);
                    }
                    newContrib.add(contributor);
                }
                config.setContributors(newContrib);
            }
        }
        
        if(properties.containsKey("Extended Ontologies Names")){
            String extended = (String)properties.get("Extended Ontologies Names");
            if(!"".equals(extended)){
                String[] extendedOntoNames = extended.split(";");
                String[] extendedURIs = null;
                ArrayList<Ontology> newExtended = new ArrayList<Ontology>();
                if(properties.containsKey("Extended Ontology URLS")){
                    if(!"".equals(properties.get("Extended Ontology URLS"))){
                        extendedURIs = ((String)properties.get("Extended Ontology URLS")).split(";");
                    }
                }
                for(int i=0; i< extendedOntoNames.length;i++){
                    Ontology o = new Ontology();
                    o.setName(extendedOntoNames[i]);
                    if(extendedURIs!=null && extendedURIs.length == extendedOntoNames.length){
                        o.setNamespaceURI(extendedURIs[i]);
                    }
                    newExtended.add(o);
                }
                config.setExtendedOntologies(newExtended);
            }
        }
        
        if(properties.containsKey("Imported Ontologies Names")){
        String imported = (String)properties.get("Imported Ontologies Names");
            if(!"".equals(imported)){
                String[] importedOntoNames = imported.split(";");
                String[] importedURIs = null;
                ArrayList<Ontology> newExtended = new ArrayList<Ontology>();
                if(properties.containsKey("Imported Ontology URLS")){
                    if(!"".equals(properties.get("Imported Ontology URLS"))){
                        importedURIs = ((String)properties.get("Imported Ontology URLS")).split(";");
                    }
                }
                for(int i=0; i< importedOntoNames.length;i++){
                    Ontology o = new Ontology();
                    o.setName(importedOntoNames[i]);
                    if(importedURIs!=null && importedURIs.length == importedOntoNames.length){
                        o.setNamespaceURI(importedURIs[i]);
                    }
                    newExtended.add(o);
                }
                config.setImportedOntologies(newExtended);
            }    
        }
    }
    
    public void saveLODEConfig(){
        
    }
    
    public void generateSkeleton() {
        //if state is initial, then it is a skeleton
        //call a method in createResources called createSkeleton, and done.
    }
    
    public void startGeneratingDoc() {
        Runnable r = new CreateDocInThread(this.config, this, this.tmpFile);
        new Thread(r).start();
    }
    
    //The other method could call directly switch state, but htis way the flow is more clear.
    public void docGenerated(String status){
        this.switchState(status);
    }
    
    private void exit(){
        this.gui.dispose();
        //delete tmp folder here!
        String[]entries = tmpFile.list();
        for(String s: entries){
            File currentFile = new File(tmpFile.getPath(),s);
            currentFile.delete();
        }
        tmpFile.delete();
    }
    
    public void switchState(String input){
        if(input.equals("cancel")){
            state = State.exit;
            exit();
        }
        switch(this.state){
            case initial:
                if(input.equals("skeleton")){
                    state = State.generated;
                    this.generateSkeleton();//TO DO
                    this.gui.dispose();
                    gui = new GuiStep5(this, true);
                    gui.setVisible(true);
                }
                else {//next
                    state = State.metadata;
                    this.gui.dispose();
                    gui = new GuiStep2(this);
                    gui.setVisible(true);
                }
                break;
            case metadata:
                if(input.equals("back")){
                    state = State.initial;
                    this.gui.dispose();
                    gui = new GuiStep1(this);
                    gui.setVisible(true);
                }else{//next
                    state = State.sections;
                    this.gui.dispose();
                    gui = new GuiStep3(this);
                    gui.setVisible(true);
                }
                break;
            case sections:
                if(input.equals("back")){
                    state = State.metadata;
                    this.gui.dispose();
                    gui = new GuiStep2(this);
                    gui.setVisible(true);
                }else{//next
                    state = State.configLODE;
                    this.gui.dispose();
                    gui = new GuiStep4(this);
                    gui.setVisible(true);
                }
                break;
            case configLODE:
                if(input.equals("back")){
                    state = State.sections;
                    this.gui.dispose();
                    gui = new GuiStep3(this);
                    gui.setVisible(true);
                }else{//next
                    state = State.loading;
                    this.startGeneratingDoc();
                }
                break;
            case loading:
                state = State.generated;
                this.gui.dispose();
                gui = new GuiStep5(this,false);
                gui.setVisible(true);
                if(input.equals("error")){
                    JOptionPane.showMessageDialog(gui,"error while generating the documentation! refine this error.");
                }
                break;                
            case generated:
                if(input.equals("restart")){
                    this.gui.dispose();
                    state = State.initial;
                    gui = new GuiStep1(this);
                    gui.setVisible(true);
                    //probably will have to associate a restart action
                }
                break;
            case exit: //exit is an abstract state. Nothing should happen here
                break;
        }
    }
    
    public void openBrowser (URI uri){
        if(Desktop.isDesktopSupported())
        {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                System.err.println("Could not open browser: "+ex.getMessage());
            }
        }
    }
    
//    public void generateDoc(boolean considerImportedOntologies, boolean considerImportedClosure, boolean useReasoner){
//        //this method will invoke the LODE transformation to get the html and then get the resultant html for our needs.
//    }
    
    public static void main(String[] args){
        new GuiController();
    }
    
    

}
