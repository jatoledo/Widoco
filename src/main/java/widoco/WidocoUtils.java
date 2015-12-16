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

package widoco;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Some useful methods reused across different classes
 * @author Daniel Garijo
 */
public class WidocoUtils {
    public static OntModel loadModel(Configuration c){
        OntModel model = ModelFactory.createOntologyModel();//ModelFactory.createDefaultModel();
        if(c.isFromFile()){
            readModel(model, c.getOntologyPath(), null);
        }else{
            //System.out.println("Ont URI: "+c.getOntologyURI());
            readModel(model, null, c.getOntologyURI());
        }
        return model;
    }
    private static void readModel(OntModel model,String ontoPath, String ontoURL){
        if(ontoPath!=null){
            InputStream in = null;
            try{
                in = FileManager.get().open(ontoPath);
                if (in == null) {
                    System.err.println("Error: Ontology file not found");
                    return;
                }
                model.read(in, null, "RDF/XML");
            }catch(Exception e){
                System.err.println("Could not load the ontology in rdf/xml. Attempting to read it in turtle...");
                try{
                    if(in!=null){
                        in.close();
                    }
                    in = FileManager.get().open(ontoPath);
                    model.read(in, null, "TURTLE");
                }catch(Exception e1){
                    System.err.println("Could not load ontology in turtle.");
                }
            }
        }else{
            try{
                model.read(ontoURL, null, "RDF/XML");
            }catch(Exception e){
                model.read(ontoURL, null, "TURTLE");
            }
        }
    }
    
    public static void copyResourceFolder(String[] resources, String savePath) throws IOException{
        for (String resource : resources) {
            String aux = resource.substring(resource.lastIndexOf("/") + 1, resource.length());
            File b = new File(savePath+File.separator+aux);
            b.createNewFile();
            copyLocalResource(resource, b);
        }
    }
    
    /**
     * Method used to copy the local files: styles, images, etc.
     * @param resourceName Name of the resource
     * @param dest file where we should copy it.
     * @throws IOException 
     */
    public static void copyLocalResource(String resourceName, File dest)  {
        try{
            copy(CreateResources.class.getResourceAsStream(resourceName), dest);
        }catch(Exception e){
            System.out.println("Exception while copying "+resourceName+" - "+e.getMessage());
        }
    }
    
    /**
     * Copy a file from outside the project into the desired file.
     * @param path
     * @param dest 
     */
    public static void copyExternalResource(String path, File dest) {
        try{
            InputStream is = new FileInputStream(path);
            copy(is, dest);
        }catch(Exception e){
            System.err.println("Exception while copying "+path+e.getMessage());
        }
    }
    
    /**
     * Code to unzip a file. Inspired from
     * http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
     * Taken from 
     * @param resourceName
     * @param outputFolder 
     */
    public static void unZipIt(String resourceName, String outputFolder){
 
     byte[] buffer = new byte[1024];
 
     try{
    	ZipInputStream zis = 
    		new ZipInputStream(CreateResources.class.getResourceAsStream(resourceName));
    	ZipEntry ze = zis.getNextEntry();
 
    	while(ze!=null){
 
    	   String fileName = ze.getName();
           File newFile = new File(outputFolder + File.separator + fileName);
//           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
           if (ze.isDirectory()){
                String temp = newFile.getAbsolutePath();
                new File(temp).mkdirs();
           }
           else{
                FileOutputStream fos = new FileOutputStream(newFile);
                int len; while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len); }
                fos.close();
           }  
            ze = zis.getNextEntry();
    	}
 
        zis.closeEntry();
    	zis.close();
 
    }catch(IOException ex){
        System.err.println("Error while extracting the reosurces: "+ex.getMessage());
    }
   } 
    
    public static void copy(InputStream is, File dest)throws Exception{
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
        catch(Exception e){
            System.err.println("Exception while copying resource. "+e.getMessage());
            throw e;
        }
        finally {
            if(is!=null)is.close();
            if(os!=null)os.close();
        }
    }

}
