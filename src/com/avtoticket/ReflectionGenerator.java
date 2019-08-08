/*
 * Copyright Бездна (c) 2012.
 */
package com.avtoticket;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.avtoticket.shared.models.BaseModel;
import com.avtoticket.shared.models.Reflection;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author Minu <<a href="minu-moto@mail.ru">minu-moto@mail.ru</a>>
 * @since 27.12.2012 17:27:48
 */
public class ReflectionGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();

		JClassType instantiableType = oracle.findType(BaseModel.class.getName());

		List<JClassType> clazzes = new ArrayList<JClassType>();

		for (JClassType classType : oracle.getTypes())
			if (/*!classType.equals(instantiableType) && */classType.isAssignableTo(instantiableType))
				clazzes.add(classType);

		final String genPackageName = BaseModel.getRootPackage();
		final String genClassName = "ReflectionImpl";

		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
				genPackageName, genClassName);
		composer.addImport(Reflection.class.getCanonicalName());
		composer.addImplementedInterface(Reflection.class.getSimpleName());

		PrintWriter printWriter = context.tryCreate(logger, genPackageName, genClassName);

		if (printWriter != null) {
			SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
			sourceWriter.println();
			sourceWriter.println("ReflectionImpl() { }");

			printFactoryMethod(clazzes, sourceWriter);

			sourceWriter.commit(logger);
		}
		return composer.getCreatedClassName();
	}

	private void printFactoryMethod(List<JClassType> clazzes, SourceWriter sourceWriter) {
		sourceWriter.println();

		sourceWriter.println("public <T extends BaseModel> T instantiate(String className) {");
		sourceWriter.indent();

		if ((clazzes != null) && !clazzes.isEmpty()) {
			sourceWriter.println("switch (className) {");
			for (JClassType classType : clazzes) {
				if (classType.isAbstract())
					continue;

				String cn = classType.getQualifiedSourceName();
				sourceWriter.println("case \"" + cn + "\":");
				sourceWriter.indentln("return (T) new " + cn + "();");
			}
			sourceWriter.println("default:");
			sourceWriter.indentln("return (T) null;");
			sourceWriter.println("}");
		}
		sourceWriter.outdent();
		sourceWriter.println("}");
	}

}