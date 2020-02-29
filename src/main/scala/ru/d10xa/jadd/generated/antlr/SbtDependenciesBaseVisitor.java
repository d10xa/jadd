// Generated from SbtDependencies.g4 by ANTLR 4.8
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;


public class SbtDependenciesBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements SbtDependenciesVisitor<T> {
	
	@Override public T visitPercents(SbtDependenciesParser.PercentsContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx) { return visitChildren(ctx); }
}