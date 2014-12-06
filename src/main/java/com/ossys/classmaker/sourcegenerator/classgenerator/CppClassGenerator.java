package com.ossys.classmaker.sourcegenerator.classgenerator;

import java.io.File;
import java.util.ArrayList;

import com.ossys.classmaker.sourcegenerator.attributegenerator.CppAttributeGenerator;
import com.ossys.classmaker.sourcegenerator.attributegenerator.AttributeGenerator.AttributeVisibilityType;
import com.ossys.classmaker.sourcegenerator.attributegenerator.CppAttributeGenerator.AttributeType;
import com.ossys.classmaker.sourcegenerator.methodgenerator.CppMethodGenerator;
import com.ossys.classmaker.sourcegenerator.methodgenerator.CppMethodGenerator.MethodType;
import com.ossys.classmaker.sourcegenerator.methodgenerator.MethodGenerator.MethodVisibilityType;

public class CppClassGenerator extends ClassGenerator {

	public static enum LibraryType {
		STANDARD,
		CUSTOM
	}
	
	StringBuilder sb_h = new StringBuilder();
	StringBuilder sb_i = new StringBuilder();
	
	String root_path = "";
	String include_path = "";
	String include_prepend = "";
	MethodVisibilityType default_constructor_visibility = MethodVisibilityType.PUBLIC;
	
	private ArrayList<CppMethodGenerator> methods = new ArrayList<CppMethodGenerator>();
	private ArrayList<CppAttributeGenerator> attributes = new ArrayList<CppAttributeGenerator>();
	private ArrayList<String> standard_header_libraries = new ArrayList<String>();
	private ArrayList<String> custom_header_libraries = new ArrayList<String>();
	private ArrayList<String> standard_implementation_libraries = new ArrayList<String>();
	private ArrayList<String> custom_implementation_libraries = new ArrayList<String>();
	private ArrayList<String> namespaces = new ArrayList<String>();
	
	public CppClassGenerator(String name, String path) {
		super(name,path);
		this.root_path = path;
	}
	
	public void addMethod(CppMethodGenerator cmg) {
		this.methods.add(cmg);
	}
	
	public void addAttribute(CppAttributeGenerator cag) {
		this.attributes.add(cag);
	}
	
	public void addHeaderLibrary(LibraryType libraryType, String library) {
		if(libraryType == LibraryType.STANDARD) {
			this.standard_header_libraries.add(library);
		} else if(libraryType == LibraryType.CUSTOM) {
			this.custom_header_libraries.add(library);
		}
	}
	
	public void addImplementationLibrary(LibraryType libraryType, String library) {
		if(libraryType == LibraryType.STANDARD) {
			this.standard_implementation_libraries.add(library);
		} else if(libraryType == LibraryType.CUSTOM) {
			this.custom_implementation_libraries.add(library);
		}
	}
	
	public void addHeaderLibrary(String library) {
		this.standard_header_libraries.add(library);
	}
	
	public void addImplementationLibrary(String library) {
		this.standard_implementation_libraries.add(library);
	}
	
	public void addNamespace(String namespace) {
		this.namespaces.add(namespace);
	}
	
	public String getNamespace() {
		StringBuilder sb = new StringBuilder();
		for(String namespace : this.namespaces) {
			sb.append(namespace + "::");
		}
		return sb.toString();
	}
	
	private String getNamespaceTabs() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<this.namespaces.size();i++) {
			sb.append("\t");
		}
		return sb.toString();
	}
	
	public String getSource() {
		this.generate();
		return this.sb_h.toString() + this.sb_i.toString();
	}
	
	public boolean hasMembersOfVisibilityType(AttributeVisibilityType type) {
		for(CppAttributeGenerator attribute : this.attributes) {
			if(attribute.getVisibilityType() == type) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<CppAttributeGenerator> getAttributesOfVisibilityType(AttributeVisibilityType type) {
		ArrayList<CppAttributeGenerator> attributes = new ArrayList<CppAttributeGenerator>();
		for(CppAttributeGenerator attribute : this.attributes) {
			if(attribute.getVisibilityType() == type) {
				attributes.add(attribute);
			}
		}
		return attributes;
	}
	
	public boolean hasMethodsOfVisibilityType(MethodVisibilityType type) {
		for(CppMethodGenerator method : this.methods) {
			if(method.getVisibilityType() == type) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<CppMethodGenerator> getMethodsOfVisibilityType(MethodVisibilityType type) {
		ArrayList<CppMethodGenerator> methods = new ArrayList<CppMethodGenerator>();
		for(CppMethodGenerator method : this.methods) {
			if(method.getVisibilityType() == type) {
				methods.add(method);
			}
		}
		return methods;
	}
	
	public void setDefaultConstructorVisibility(MethodVisibilityType type) {
		this.default_constructor_visibility = type;
	}
	
	public void setIncludePath(String include_path) {
		this.include_path = include_path;
	}
	
	public void setIncludePrepend(String include_prepend) {
		this.include_prepend = include_prepend;
	}
	
	public String getHeaderSource() {
		this.generate();
		return this.sb_h.toString();
	}
	
	public String getImplementationSource() {
		this.generate();
		return this.sb_i.toString();
	}
	
	private void generate() {
		this.sb_h = new StringBuilder();
		this.sb_i = new StringBuilder();
		
		//Creating Default Constructor Method
		CppMethodGenerator constructor = new CppMethodGenerator(this.name);
		constructor.setVisibility(this.default_constructor_visibility);
		constructor.setConstructor();
		constructor.setNamespace(this.getNamespace());
		constructor.setClassname(this.name);
		
		//Creating Default Destructor Method
		CppMethodGenerator destructor = new CppMethodGenerator(this.name);
		destructor.setVisibility(MethodVisibilityType.PUBLIC);
		destructor.setVirtual();
		destructor.setDestructor();
		destructor.setNamespace(this.getNamespace());
		destructor.setClassname(this.name);
		
		//Header File
		this.sb_h.append("#ifndef _" + this.name.toUpperCase() + "_H_\n");
		this.sb_h.append("#define _" + this.name.toUpperCase() + "_H_\n\n");


		for(String standard_library : this.standard_header_libraries) {
			this.sb_h.append("#include <" + standard_library + ">\n");
		}
		for(String custom_library : this.custom_header_libraries) {
			this.sb_h.append("#include \"" + custom_library + "\"\n");
		}
		if(this.custom_header_libraries.size() > 0) {
			this.sb_h.append("\n");
		}
		
		// Defining Namespaces
		int ns_cnt=0;
		for(String namespace : this.namespaces) {
			for(int i=0;i<ns_cnt;i++) {
				this.sb_h.append("\t");
			}
			this.sb_h.append("namespace " + namespace + " {\n");
			ns_cnt++;
		}
		if(this.namespaces.size() > 0) {
			this.sb_h.append("\n");
		}
		// Begin Class Definition
		this.sb_h.append(this.getNamespaceTabs() + "class " + this.name);
		if(this.extended_classes.size() > 0) {
			this.sb_h.append(" : ");
			int cnt=0;
			for(String extended_class : this.extended_classes) {
				if(cnt > 0) {
					this.sb_h.append(", ");
				}
				this.sb_h.append("public " + extended_class);
				cnt++;
			}
		}
		this.sb_h.append(" {\n\n");
		
		// Adding private attributes and methods
		if(this.default_constructor_visibility == MethodVisibilityType.PRIVATE || 
				this.hasMethodsOfVisibilityType(MethodVisibilityType.PRIVATE) ||
				this.hasMembersOfVisibilityType(AttributeVisibilityType.PRIVATE)) {
			this.sb_h.append(this.getNamespaceTabs() + "private:\n");
			
			boolean newline = true;
			for(CppAttributeGenerator attribute : this.getAttributesOfVisibilityType(AttributeVisibilityType.PRIVATE)) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + attribute.getSource(AttributeType.DEFINITION));
				newline = true;
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}

			// Adding Default Constructor to Header
			if(this.default_constructor_visibility == MethodVisibilityType.PRIVATE) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + constructor.getSource(MethodType.DEFINITION));
				newline = true;
			}
			// Get Non-Default Constructors
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PRIVATE)) {
				if(method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
					newline = true;
				}
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}

			// Get Remaining Methods
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PRIVATE)) {
				if(!method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
					newline = true;
				}
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}
		}

		// Adding protected attributes and methods
		if(this.default_constructor_visibility == MethodVisibilityType.PROTECTED || 
				this.hasMethodsOfVisibilityType(MethodVisibilityType.PROTECTED) ||
				this.hasMembersOfVisibilityType(AttributeVisibilityType.PROTECTED)) {
			this.sb_h.append(this.getNamespaceTabs() + "protected:\n");
			
			boolean newline = false;
			for(CppAttributeGenerator attribute : this.getAttributesOfVisibilityType(AttributeVisibilityType.PROTECTED)) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + attribute.getSource(AttributeType.DEFINITION));
				newline = true;
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}

			// Adding Default Constructor to Header
			if(this.default_constructor_visibility == MethodVisibilityType.PROTECTED) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + constructor.getSource(MethodType.DEFINITION));
				newline = true;
			}
			// Get Non-Default Constructors
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PROTECTED)) {
				if(method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
					newline = true;
				}
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}

			// Get Remaining Methods
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PROTECTED)) {
				if(!method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
					newline = true;
				}
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}
		}
		
		// Adding public attributes and methods
		if(this.default_constructor_visibility == MethodVisibilityType.PUBLIC || 
			this.hasMethodsOfVisibilityType(MethodVisibilityType.PUBLIC) ||
			this.hasMembersOfVisibilityType(AttributeVisibilityType.PUBLIC)) {
			this.sb_h.append(this.getNamespaceTabs() + "public:\n");
			
			boolean newline = false;
			for(CppAttributeGenerator attribute : this.getAttributesOfVisibilityType(AttributeVisibilityType.PUBLIC)) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + attribute.getSource(AttributeType.DEFINITION));
				newline = true;
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}

			// Adding Default Constructor to Header
			if(this.default_constructor_visibility == MethodVisibilityType.PUBLIC) {
				this.sb_h.append(this.getNamespaceTabs() + "\t" + constructor.getSource(MethodType.DEFINITION));
			}
			// Get Non-Default Constructors
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PUBLIC)) {
				if(method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
				}
			}
			
			// Adding Destructor to Header
			this.sb_h.append(this.getNamespaceTabs() + "\t" + destructor.getSource(MethodType.DEFINITION));
			this.sb_h.append("\n");
			
			// Get Remaining Methods
			for(CppMethodGenerator method : this.getMethodsOfVisibilityType(MethodVisibilityType.PUBLIC)) {
				if(!method.isConstructor()) {
					this.sb_h.append(this.getNamespaceTabs() + "\t" + method.getSource(MethodType.DEFINITION));
					newline = true;
				}
			}
			if(newline) {
				this.sb_h.append("\n");
				newline = false;
			}
		}
		
		this.sb_h.append(this.getNamespaceTabs() + "};\n\n");
		// End Class Definition
		
		
		for(; ns_cnt>0;ns_cnt--) {
			for(int i=1;i<ns_cnt;i++) {
				this.sb_h.append("\t");
			}
			this.sb_h.append("}\n");
		}
		
		this.sb_h.append("#endif /* _" + this.name.toUpperCase() + "_H_ */\n");
		

		
		//CPP File
		//Include Header File
		this.sb_i.append("#include \"" + this.include_prepend + "/" + this.name + ".h\"\n\n");

		for(CppAttributeGenerator attribute : CppAttributeGenerator.getGlobals()) {
			attribute.setNamespace(this.getNamespace());
			attribute.setClassname(this.name);
			this.sb_i.append(attribute.getSource(AttributeType.GLOBAL));
		}
		if(CppAttributeGenerator.getGlobals().size() > 0) {
			this.sb_i.append("\n");
		}

		for(String standard_library : this.standard_implementation_libraries) {
			this.sb_i.append("#include <" + standard_library + ">\n");
		}
		for(String custom_library : this.custom_implementation_libraries) {
			this.sb_i.append("#include \"" + custom_library + "\"\n");
		}
		if(this.custom_implementation_libraries.size() > 0) {
			this.sb_i.append("\n");
		}
		
		this.sb_i.append(constructor.getSource(MethodType.IMPLEMENTATION));
		
		for(CppMethodGenerator method : this.methods) {
			if(method.isConstructor()) {
				method.setNamespace(this.getNamespace());
				method.setClassname(this.name);
				this.sb_i.append(method.getSource(MethodType.IMPLEMENTATION));
			}
		}
			
		this.sb_i.append(destructor.getSource(MethodType.IMPLEMENTATION));
		
		for(CppMethodGenerator method : this.methods) {
			if(!method.isConstructor()) {
				method.setNamespace(this.getNamespace());
				method.setClassname(this.name);
				this.sb_i.append(method.getSource(MethodType.IMPLEMENTATION));
			}
		}
	}
	
	public boolean save() {
		this.generate();

		this.sb.delete(0, this.sb.length());
		this.sb.append(this.sb_i);
		this.path = this.root_path + System.getProperty("file.separator") + ClassGenerator.getName(this.name,NamingSyntaxType.PASCAL, false) + ".cpp";
		
		if(super.save()) {
			File file = new File(this.root_path + System.getProperty("file.separator") + "include");
			if(!file.exists()) {
				file.mkdir();
			}
			
			this.sb.delete(0, this.sb.length());
			this.sb.append(this.sb_h);
			this.path = this.include_path + System.getProperty("file.separator") + ClassGenerator.getName(this.name,NamingSyntaxType.PASCAL, false) + ".h";
			
			if(super.save()) {
				this.sb.delete(0, this.sb.length());
			} else {
				return false;
			}
		} else {
			return false;
		}
		
		return true;
	}
	
}
