// Generated from SbtDependencies.g4 by ANTLR 4.7.2
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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, Scope=12, ScalaString=13, WS=14, Character=15, NEWLINE=16, 
		COMMENT=17, LINE_COMMENT=18;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "Scope", "ScalaString", "WS", "Character", "NEWLINE", 
			"COMMENT", "LINE_COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'%'", "'%%'", "','", "'libraryDependencies'", "'++='", "'Seq'", 
			"'List'", "'Vector'", "'('", "')'", "'+='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"Scope", "ScalaString", "WS", "Character", "NEWLINE", "COMMENT", "LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\24\u0098\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6"+
		"\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3"+
		"\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\rh"+
		"\n\r\3\16\3\16\7\16l\n\16\f\16\16\16o\13\16\3\16\3\16\3\17\3\17\3\17\3"+
		"\17\3\20\3\20\3\21\6\21z\n\21\r\21\16\21{\3\21\3\21\3\22\3\22\3\22\3\22"+
		"\7\22\u0084\n\22\f\22\16\22\u0087\13\22\3\22\3\22\3\22\3\22\3\22\3\23"+
		"\3\23\3\23\3\23\7\23\u0092\n\23\f\23\16\23\u0095\13\23\3\23\3\23\3\u0085"+
		"\2\24\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\3\2\5\5\2\13\f\17\17\"\"\4\2\"#%\u0081\4\2\f\f\17"+
		"\17\2\u009c\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5)\3\2\2\2\7,\3\2\2\2\t.\3\2\2\2"+
		"\13B\3\2\2\2\rF\3\2\2\2\17J\3\2\2\2\21O\3\2\2\2\23V\3\2\2\2\25X\3\2\2"+
		"\2\27Z\3\2\2\2\31g\3\2\2\2\33i\3\2\2\2\35r\3\2\2\2\37v\3\2\2\2!y\3\2\2"+
		"\2#\177\3\2\2\2%\u008d\3\2\2\2\'(\7\'\2\2(\4\3\2\2\2)*\7\'\2\2*+\7\'\2"+
		"\2+\6\3\2\2\2,-\7.\2\2-\b\3\2\2\2./\7n\2\2/\60\7k\2\2\60\61\7d\2\2\61"+
		"\62\7t\2\2\62\63\7c\2\2\63\64\7t\2\2\64\65\7{\2\2\65\66\7F\2\2\66\67\7"+
		"g\2\2\678\7r\2\289\7g\2\29:\7p\2\2:;\7f\2\2;<\7g\2\2<=\7p\2\2=>\7e\2\2"+
		">?\7k\2\2?@\7g\2\2@A\7u\2\2A\n\3\2\2\2BC\7-\2\2CD\7-\2\2DE\7?\2\2E\f\3"+
		"\2\2\2FG\7U\2\2GH\7g\2\2HI\7s\2\2I\16\3\2\2\2JK\7N\2\2KL\7k\2\2LM\7u\2"+
		"\2MN\7v\2\2N\20\3\2\2\2OP\7X\2\2PQ\7g\2\2QR\7e\2\2RS\7v\2\2ST\7q\2\2T"+
		"U\7t\2\2U\22\3\2\2\2VW\7*\2\2W\24\3\2\2\2XY\7+\2\2Y\26\3\2\2\2Z[\7-\2"+
		"\2[\\\7?\2\2\\\30\3\2\2\2]^\7V\2\2^_\7g\2\2_`\7u\2\2`h\7v\2\2ab\7$\2\2"+
		"bc\7v\2\2cd\7g\2\2de\7u\2\2ef\7v\2\2fh\7$\2\2g]\3\2\2\2ga\3\2\2\2h\32"+
		"\3\2\2\2im\7$\2\2jl\5\37\20\2kj\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2"+
		"np\3\2\2\2om\3\2\2\2pq\7$\2\2q\34\3\2\2\2rs\t\2\2\2st\3\2\2\2tu\b\17\2"+
		"\2u\36\3\2\2\2vw\t\3\2\2w \3\2\2\2xz\7\f\2\2yx\3\2\2\2z{\3\2\2\2{y\3\2"+
		"\2\2{|\3\2\2\2|}\3\2\2\2}~\b\21\2\2~\"\3\2\2\2\177\u0080\7\61\2\2\u0080"+
		"\u0081\7,\2\2\u0081\u0085\3\2\2\2\u0082\u0084\13\2\2\2\u0083\u0082\3\2"+
		"\2\2\u0084\u0087\3\2\2\2\u0085\u0086\3\2\2\2\u0085\u0083\3\2\2\2\u0086"+
		"\u0088\3\2\2\2\u0087\u0085\3\2\2\2\u0088\u0089\7,\2\2\u0089\u008a\7\61"+
		"\2\2\u008a\u008b\3\2\2\2\u008b\u008c\b\22\2\2\u008c$\3\2\2\2\u008d\u008e"+
		"\7\61\2\2\u008e\u008f\7\61\2\2\u008f\u0093\3\2\2\2\u0090\u0092\n\4\2\2"+
		"\u0091\u0090\3\2\2\2\u0092\u0095\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094"+
		"\3\2\2\2\u0094\u0096\3\2\2\2\u0095\u0093\3\2\2\2\u0096\u0097\b\23\2\2"+
		"\u0097&\3\2\2\2\b\2gm{\u0085\u0093\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}