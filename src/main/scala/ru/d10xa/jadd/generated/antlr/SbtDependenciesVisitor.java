// Generated from SbtDependencies.g4 by ANTLR 4.8
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;


public interface SbtDependenciesVisitor<T> extends ParseTreeVisitor<T> {
	
	T visitPercents(SbtDependenciesParser.PercentsContext ctx);
	
	T visitSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	
	T visitMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	
	T visitLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	
	T visitLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
}