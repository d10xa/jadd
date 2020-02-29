// Generated from SbtDependencies.g4 by ANTLR 4.8
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;


public interface SbtDependenciesListener extends ParseTreeListener {
	
	void enterPercents(SbtDependenciesParser.PercentsContext ctx);
	
	void exitPercents(SbtDependenciesParser.PercentsContext ctx);
	
	void enterSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	
	void exitSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	
	void enterMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	
	void exitMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	
	void enterLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	
	void exitLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	
	void enterLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
	
	void exitLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
}