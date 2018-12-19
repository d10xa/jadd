// Generated from SbtDependencies.g4 by ANTLR 4.7.1
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SbtDependenciesParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, ScalaString=13, WS=14, Character=15, NEWLINE=16, 
		COMMENT=17, LINE_COMMENT=18;
	public static final int
		RULE_percents = 0, RULE_singleDependency = 1, RULE_multipleDependencies = 2, 
		RULE_libraryDependencies = 3, RULE_libraryDependency = 4;
	public static final String[] ruleNames = {
		"percents", "singleDependency", "multipleDependencies", "libraryDependencies", 
		"libraryDependency"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'%'", "'%%'", "'Test'", "','", "'libraryDependencies'", "'++='", 
		"'Seq'", "'List'", "'Vector'", "'('", "')'", "'+='"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, "ScalaString", "WS", "Character", "NEWLINE", "COMMENT", "LINE_COMMENT"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SbtDependencies.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SbtDependenciesParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class PercentsContext extends ParserRuleContext {
		public PercentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_percents; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).enterPercents(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).exitPercents(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SbtDependenciesVisitor ) return ((SbtDependenciesVisitor<? extends T>)visitor).visitPercents(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PercentsContext percents() throws RecognitionException {
		PercentsContext _localctx = new PercentsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_percents);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			_la = _input.LA(1);
			if ( !(_la==T__0 || _la==T__1) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleDependencyContext extends ParserRuleContext {
		public List<TerminalNode> ScalaString() { return getTokens(SbtDependenciesParser.ScalaString); }
		public TerminalNode ScalaString(int i) {
			return getToken(SbtDependenciesParser.ScalaString, i);
		}
		public PercentsContext percents() {
			return getRuleContext(PercentsContext.class,0);
		}
		public SingleDependencyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleDependency; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).enterSingleDependency(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).exitSingleDependency(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SbtDependenciesVisitor ) return ((SbtDependenciesVisitor<? extends T>)visitor).visitSingleDependency(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleDependencyContext singleDependency() throws RecognitionException {
		SingleDependencyContext _localctx = new SingleDependencyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_singleDependency);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			match(ScalaString);
			setState(13);
			percents();
			setState(14);
			match(ScalaString);
			setState(15);
			match(T__0);
			setState(16);
			match(ScalaString);
			setState(19);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(17);
				match(T__0);
				setState(18);
				match(T__2);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipleDependenciesContext extends ParserRuleContext {
		public List<SingleDependencyContext> singleDependency() {
			return getRuleContexts(SingleDependencyContext.class);
		}
		public SingleDependencyContext singleDependency(int i) {
			return getRuleContext(SingleDependencyContext.class,i);
		}
		public MultipleDependenciesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipleDependencies; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).enterMultipleDependencies(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).exitMultipleDependencies(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SbtDependenciesVisitor ) return ((SbtDependenciesVisitor<? extends T>)visitor).visitMultipleDependencies(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipleDependenciesContext multipleDependencies() throws RecognitionException {
		MultipleDependenciesContext _localctx = new MultipleDependenciesContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_multipleDependencies);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			singleDependency();
			setState(26);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(22);
				match(T__3);
				setState(23);
				singleDependency();
				}
				}
				setState(28);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LibraryDependenciesContext extends ParserRuleContext {
		public MultipleDependenciesContext multipleDependencies() {
			return getRuleContext(MultipleDependenciesContext.class,0);
		}
		public LibraryDependenciesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_libraryDependencies; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).enterLibraryDependencies(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).exitLibraryDependencies(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SbtDependenciesVisitor ) return ((SbtDependenciesVisitor<? extends T>)visitor).visitLibraryDependencies(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LibraryDependenciesContext libraryDependencies() throws RecognitionException {
		LibraryDependenciesContext _localctx = new LibraryDependenciesContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_libraryDependencies);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			match(T__4);
			setState(30);
			match(T__5);
			setState(31);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__6) | (1L << T__7) | (1L << T__8))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(32);
			match(T__9);
			setState(34);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ScalaString) {
				{
				setState(33);
				multipleDependencies();
				}
			}

			setState(36);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LibraryDependencyContext extends ParserRuleContext {
		public SingleDependencyContext singleDependency() {
			return getRuleContext(SingleDependencyContext.class,0);
		}
		public LibraryDependencyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_libraryDependency; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).enterLibraryDependency(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SbtDependenciesListener ) ((SbtDependenciesListener)listener).exitLibraryDependency(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SbtDependenciesVisitor ) return ((SbtDependenciesVisitor<? extends T>)visitor).visitLibraryDependency(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LibraryDependencyContext libraryDependency() throws RecognitionException {
		LibraryDependencyContext _localctx = new LibraryDependencyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_libraryDependency);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			match(T__4);
			setState(39);
			match(T__11);
			setState(40);
			singleDependency();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\24-\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\26"+
		"\n\3\3\4\3\4\3\4\7\4\33\n\4\f\4\16\4\36\13\4\3\5\3\5\3\5\3\5\3\5\5\5%"+
		"\n\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\2\2\7\2\4\6\b\n\2\4\3\2\3\4\3\2\t\13"+
		"\2*\2\f\3\2\2\2\4\16\3\2\2\2\6\27\3\2\2\2\b\37\3\2\2\2\n(\3\2\2\2\f\r"+
		"\t\2\2\2\r\3\3\2\2\2\16\17\7\17\2\2\17\20\5\2\2\2\20\21\7\17\2\2\21\22"+
		"\7\3\2\2\22\25\7\17\2\2\23\24\7\3\2\2\24\26\7\5\2\2\25\23\3\2\2\2\25\26"+
		"\3\2\2\2\26\5\3\2\2\2\27\34\5\4\3\2\30\31\7\6\2\2\31\33\5\4\3\2\32\30"+
		"\3\2\2\2\33\36\3\2\2\2\34\32\3\2\2\2\34\35\3\2\2\2\35\7\3\2\2\2\36\34"+
		"\3\2\2\2\37 \7\7\2\2 !\7\b\2\2!\"\t\3\2\2\"$\7\f\2\2#%\5\6\4\2$#\3\2\2"+
		"\2$%\3\2\2\2%&\3\2\2\2&\'\7\r\2\2\'\t\3\2\2\2()\7\7\2\2)*\7\16\2\2*+\5"+
		"\4\3\2+\13\3\2\2\2\5\25\34$";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}