// Generated from SbtDependencies.g4 by ANTLR 4.7.1
package ru.d10xa.jadd.generated.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SbtDependenciesLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, ScalaString=14, WS=15, Character=16, 
		NEWLINE=17, COMMENT=18, LINE_COMMENT=19;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "ScalaString", "WS", "Character", "NEWLINE", 
		"COMMENT", "LINE_COMMENT"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'%'", "'%%'", "'Test'", "'\"test\"'", "','", "'libraryDependencies'", 
		"'++='", "'Seq'", "'List'", "'Vector'", "'('", "')'", "'+='"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, "ScalaString", "WS", "Character", "NEWLINE", "COMMENT", "LINE_COMMENT"
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


	public SbtDependenciesLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SbtDependencies.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\25\u009a\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t"+
		"\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3"+
		"\r\3\r\3\16\3\16\3\16\3\17\3\17\7\17n\n\17\f\17\16\17q\13\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\3\21\3\21\3\22\6\22|\n\22\r\22\16\22}\3\22\3\22\3"+
		"\23\3\23\3\23\3\23\7\23\u0086\n\23\f\23\16\23\u0089\13\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\24\3\24\3\24\3\24\7\24\u0094\n\24\f\24\16\24\u0097\13"+
		"\24\3\24\3\24\3\u0087\2\25\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25\3\2\5\5\2\13\f\17\17"+
		"\"\"\4\2\"#%\u0081\4\2\f\f\17\17\2\u009d\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35"+
		"\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\3)"+
		"\3\2\2\2\5+\3\2\2\2\7.\3\2\2\2\t\63\3\2\2\2\13:\3\2\2\2\r<\3\2\2\2\17"+
		"P\3\2\2\2\21T\3\2\2\2\23X\3\2\2\2\25]\3\2\2\2\27d\3\2\2\2\31f\3\2\2\2"+
		"\33h\3\2\2\2\35k\3\2\2\2\37t\3\2\2\2!x\3\2\2\2#{\3\2\2\2%\u0081\3\2\2"+
		"\2\'\u008f\3\2\2\2)*\7\'\2\2*\4\3\2\2\2+,\7\'\2\2,-\7\'\2\2-\6\3\2\2\2"+
		"./\7V\2\2/\60\7g\2\2\60\61\7u\2\2\61\62\7v\2\2\62\b\3\2\2\2\63\64\7$\2"+
		"\2\64\65\7v\2\2\65\66\7g\2\2\66\67\7u\2\2\678\7v\2\289\7$\2\29\n\3\2\2"+
		"\2:;\7.\2\2;\f\3\2\2\2<=\7n\2\2=>\7k\2\2>?\7d\2\2?@\7t\2\2@A\7c\2\2AB"+
		"\7t\2\2BC\7{\2\2CD\7F\2\2DE\7g\2\2EF\7r\2\2FG\7g\2\2GH\7p\2\2HI\7f\2\2"+
		"IJ\7g\2\2JK\7p\2\2KL\7e\2\2LM\7k\2\2MN\7g\2\2NO\7u\2\2O\16\3\2\2\2PQ\7"+
		"-\2\2QR\7-\2\2RS\7?\2\2S\20\3\2\2\2TU\7U\2\2UV\7g\2\2VW\7s\2\2W\22\3\2"+
		"\2\2XY\7N\2\2YZ\7k\2\2Z[\7u\2\2[\\\7v\2\2\\\24\3\2\2\2]^\7X\2\2^_\7g\2"+
		"\2_`\7e\2\2`a\7v\2\2ab\7q\2\2bc\7t\2\2c\26\3\2\2\2de\7*\2\2e\30\3\2\2"+
		"\2fg\7+\2\2g\32\3\2\2\2hi\7-\2\2ij\7?\2\2j\34\3\2\2\2ko\7$\2\2ln\5!\21"+
		"\2ml\3\2\2\2nq\3\2\2\2om\3\2\2\2op\3\2\2\2pr\3\2\2\2qo\3\2\2\2rs\7$\2"+
		"\2s\36\3\2\2\2tu\t\2\2\2uv\3\2\2\2vw\b\20\2\2w \3\2\2\2xy\t\3\2\2y\"\3"+
		"\2\2\2z|\7\f\2\2{z\3\2\2\2|}\3\2\2\2}{\3\2\2\2}~\3\2\2\2~\177\3\2\2\2"+
		"\177\u0080\b\22\2\2\u0080$\3\2\2\2\u0081\u0082\7\61\2\2\u0082\u0083\7"+
		",\2\2\u0083\u0087\3\2\2\2\u0084\u0086\13\2\2\2\u0085\u0084\3\2\2\2\u0086"+
		"\u0089\3\2\2\2\u0087\u0088\3\2\2\2\u0087\u0085\3\2\2\2\u0088\u008a\3\2"+
		"\2\2\u0089\u0087\3\2\2\2\u008a\u008b\7,\2\2\u008b\u008c\7\61\2\2\u008c"+
		"\u008d\3\2\2\2\u008d\u008e\b\23\2\2\u008e&\3\2\2\2\u008f\u0090\7\61\2"+
		"\2\u0090\u0091\7\61\2\2\u0091\u0095\3\2\2\2\u0092\u0094\n\4\2\2\u0093"+
		"\u0092\3\2\2\2\u0094\u0097\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2"+
		"\2\2\u0096\u0098\3\2\2\2\u0097\u0095\3\2\2\2\u0098\u0099\b\24\2\2\u0099"+
		"(\3\2\2\2\7\2o}\u0087\u0095\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}