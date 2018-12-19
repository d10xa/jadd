// Generated from SbtDependencies.g4 by ANTLR 4.7.1
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SbtDependenciesParser}.
 */
public interface SbtDependenciesListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SbtDependenciesParser#percents}.
	 * @param ctx the parse tree
	 */
	void enterPercents(SbtDependenciesParser.PercentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SbtDependenciesParser#percents}.
	 * @param ctx the parse tree
	 */
	void exitPercents(SbtDependenciesParser.PercentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SbtDependenciesParser#singleDependency}.
	 * @param ctx the parse tree
	 */
	void enterSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SbtDependenciesParser#singleDependency}.
	 * @param ctx the parse tree
	 */
	void exitSingleDependency(SbtDependenciesParser.SingleDependencyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SbtDependenciesParser#multipleDependencies}.
	 * @param ctx the parse tree
	 */
	void enterMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SbtDependenciesParser#multipleDependencies}.
	 * @param ctx the parse tree
	 */
	void exitMultipleDependencies(SbtDependenciesParser.MultipleDependenciesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SbtDependenciesParser#libraryDependencies}.
	 * @param ctx the parse tree
	 */
	void enterLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SbtDependenciesParser#libraryDependencies}.
	 * @param ctx the parse tree
	 */
	void exitLibraryDependencies(SbtDependenciesParser.LibraryDependenciesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SbtDependenciesParser#libraryDependency}.
	 * @param ctx the parse tree
	 */
	void enterLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SbtDependenciesParser#libraryDependency}.
	 * @param ctx the parse tree
	 */
	void exitLibraryDependency(SbtDependenciesParser.LibraryDependencyContext ctx);
}