// Generated from SbtDependencies.g4 by ANTLR 4.7.1
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SbtDependenciesParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SbtDependenciesVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SbtDependenciesParser#percents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercents(SbtDependenciesParser.PercentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SbtDependenciesParser#singleDependency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SbtDependenciesParser#multipleDependencies}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SbtDependenciesParser#libraryDependencies}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SbtDependenciesParser#libraryDependency}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
}