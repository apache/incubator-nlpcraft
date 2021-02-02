// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, SQSTRING=15, DQSTRING=16, 
		PRED_OP=17, AND=18, OR=19, VERT=20, EXCL=21, LPAREN=22, RPAREN=23, LCURLY=24, 
		RCURLY=25, SQUOTE=26, DQUOTE=27, TILDA=28, RIGHT=29, LBR=30, RBR=31, POUND=32, 
		COMMA=33, COLON=34, MINUS=35, DOT=36, UNDERSCORE=37, EQ=38, PLUS=39, QUESTION=40, 
		STAR=41, DEVIDE=42, DOLLAR=43, POWER=44, BOOL=45, INT=46, EXP=47, ID=48, 
		WS=49, ErrorCharacter=50;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "SQSTRING", "DQSTRING", "PRED_OP", 
			"AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", 
			"SQUOTE", "DQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "POUND", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"DEVIDE", "DOLLAR", "POWER", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'null'", "'term'", 
			"'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", "'groups'", 
			"'ancestors'", "'value'", null, null, null, "'&&'", "'||'", "'|'", "'!'", 
			"'('", "')'", "'{'", "'}'", "'''", "'\"'", "'~'", "'>>'", "'['", "']'", 
			"'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", "'+'", "'?'", "'*'", 
			"'/'", "'$'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, "SQSTRING", "DQSTRING", "PRED_OP", "AND", "OR", "VERT", 
			"EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", "SQUOTE", "DQUOTE", "TILDA", 
			"RIGHT", "LBR", "RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", 
			"EQ", "PLUS", "QUESTION", "STAR", "DEVIDE", "DOLLAR", "POWER", "BOOL", 
			"INT", "EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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


	public NCIntentDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\64\u0150\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2"+
		"\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3"+
		"\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b"+
		"\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3"+
		"\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\7\20\u00c6\n\20\f\20"+
		"\16\20\u00c9\13\20\3\20\3\20\3\21\3\21\7\21\u00cf\n\21\f\21\16\21\u00d2"+
		"\13\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\5\22\u00e3\n\22\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\26"+
		"\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35"+
		"\3\35\3\36\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3"+
		"&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3.\3.\3.\3.\3."+
		"\3.\3.\5.\u0127\n.\3/\3/\3/\7/\u012c\n/\f/\16/\u012f\13/\5/\u0131\n/\3"+
		"\60\3\60\6\60\u0135\n\60\r\60\16\60\u0136\3\61\3\61\6\61\u013b\n\61\r"+
		"\61\16\61\u013c\3\61\3\61\3\61\3\61\7\61\u0143\n\61\f\61\16\61\u0146\13"+
		"\61\3\62\6\62\u0149\n\62\r\62\16\62\u014a\3\62\3\62\3\63\3\63\2\2\64\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37"+
		"\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37="+
		" ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64\3\2\13\3\2))\3\2"+
		"$$\4\2>>@@\3\2\63;\4\2\62;aa\3\2\62;\4\2C\\c|\5\2\62;C\\c|\5\2\13\f\16"+
		"\17\"\"\2\u0162\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3"+
		"\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2"+
		"\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3"+
		"\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2"+
		"\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\2"+
		"9\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3"+
		"\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2"+
		"\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2"+
		"_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\3g\3\2\2\2\5n\3\2\2\2\7v\3"+
		"\2\2\2\t{\3\2\2\2\13\u0080\3\2\2\2\r\u0085\3\2\2\2\17\u008a\3\2\2\2\21"+
		"\u008d\3\2\2\2\23\u0095\3\2\2\2\25\u009e\3\2\2\2\27\u00a5\3\2\2\2\31\u00ac"+
		"\3\2\2\2\33\u00b3\3\2\2\2\35\u00bd\3\2\2\2\37\u00c3\3\2\2\2!\u00cc\3\2"+
		"\2\2#\u00e2\3\2\2\2%\u00e4\3\2\2\2\'\u00e7\3\2\2\2)\u00ea\3\2\2\2+\u00ec"+
		"\3\2\2\2-\u00ee\3\2\2\2/\u00f0\3\2\2\2\61\u00f2\3\2\2\2\63\u00f4\3\2\2"+
		"\2\65\u00f6\3\2\2\2\67\u00f8\3\2\2\29\u00fa\3\2\2\2;\u00fc\3\2\2\2=\u00ff"+
		"\3\2\2\2?\u0101\3\2\2\2A\u0103\3\2\2\2C\u0105\3\2\2\2E\u0107\3\2\2\2G"+
		"\u0109\3\2\2\2I\u010b\3\2\2\2K\u010d\3\2\2\2M\u010f\3\2\2\2O\u0111\3\2"+
		"\2\2Q\u0113\3\2\2\2S\u0115\3\2\2\2U\u0117\3\2\2\2W\u0119\3\2\2\2Y\u011b"+
		"\3\2\2\2[\u0126\3\2\2\2]\u0130\3\2\2\2_\u0132\3\2\2\2a\u013a\3\2\2\2c"+
		"\u0148\3\2\2\2e\u014e\3\2\2\2gh\7k\2\2hi\7p\2\2ij\7v\2\2jk\7g\2\2kl\7"+
		"p\2\2lm\7v\2\2m\4\3\2\2\2no\7q\2\2op\7t\2\2pq\7f\2\2qr\7g\2\2rs\7t\2\2"+
		"st\7g\2\2tu\7f\2\2u\6\3\2\2\2vw\7h\2\2wx\7n\2\2xy\7q\2\2yz\7y\2\2z\b\3"+
		"\2\2\2{|\7o\2\2|}\7g\2\2}~\7v\2\2~\177\7c\2\2\177\n\3\2\2\2\u0080\u0081"+
		"\7p\2\2\u0081\u0082\7w\2\2\u0082\u0083\7n\2\2\u0083\u0084\7n\2\2\u0084"+
		"\f\3\2\2\2\u0085\u0086\7v\2\2\u0086\u0087\7g\2\2\u0087\u0088\7t\2\2\u0088"+
		"\u0089\7o\2\2\u0089\16\3\2\2\2\u008a\u008b\7k\2\2\u008b\u008c\7f\2\2\u008c"+
		"\20\3\2\2\2\u008d\u008e\7c\2\2\u008e\u008f\7n\2\2\u008f\u0090\7k\2\2\u0090"+
		"\u0091\7c\2\2\u0091\u0092\7u\2\2\u0092\u0093\7g\2\2\u0093\u0094\7u\2\2"+
		"\u0094\22\3\2\2\2\u0095\u0096\7u\2\2\u0096\u0097\7v\2\2\u0097\u0098\7"+
		"c\2\2\u0098\u0099\7t\2\2\u0099\u009a\7v\2\2\u009a\u009b\7k\2\2\u009b\u009c"+
		"\7f\2\2\u009c\u009d\7z\2\2\u009d\24\3\2\2\2\u009e\u009f\7g\2\2\u009f\u00a0"+
		"\7p\2\2\u00a0\u00a1\7f\2\2\u00a1\u00a2\7k\2\2\u00a2\u00a3\7f\2\2\u00a3"+
		"\u00a4\7z\2\2\u00a4\26\3\2\2\2\u00a5\u00a6\7r\2\2\u00a6\u00a7\7c\2\2\u00a7"+
		"\u00a8\7t\2\2\u00a8\u00a9\7g\2\2\u00a9\u00aa\7p\2\2\u00aa\u00ab\7v\2\2"+
		"\u00ab\30\3\2\2\2\u00ac\u00ad\7i\2\2\u00ad\u00ae\7t\2\2\u00ae\u00af\7"+
		"q\2\2\u00af\u00b0\7w\2\2\u00b0\u00b1\7r\2\2\u00b1\u00b2\7u\2\2\u00b2\32"+
		"\3\2\2\2\u00b3\u00b4\7c\2\2\u00b4\u00b5\7p\2\2\u00b5\u00b6\7e\2\2\u00b6"+
		"\u00b7\7g\2\2\u00b7\u00b8\7u\2\2\u00b8\u00b9\7v\2\2\u00b9\u00ba\7q\2\2"+
		"\u00ba\u00bb\7t\2\2\u00bb\u00bc\7u\2\2\u00bc\34\3\2\2\2\u00bd\u00be\7"+
		"x\2\2\u00be\u00bf\7c\2\2\u00bf\u00c0\7n\2\2\u00c0\u00c1\7w\2\2\u00c1\u00c2"+
		"\7g\2\2\u00c2\36\3\2\2\2\u00c3\u00c7\5\65\33\2\u00c4\u00c6\n\2\2\2\u00c5"+
		"\u00c4\3\2\2\2\u00c6\u00c9\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c7\u00c8\3\2"+
		"\2\2\u00c8\u00ca\3\2\2\2\u00c9\u00c7\3\2\2\2\u00ca\u00cb\5\65\33\2\u00cb"+
		" \3\2\2\2\u00cc\u00d0\5\67\34\2\u00cd\u00cf\n\3\2\2\u00ce\u00cd\3\2\2"+
		"\2\u00cf\u00d2\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d3"+
		"\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d3\u00d4\5\67\34\2\u00d4\"\3\2\2\2\u00d5"+
		"\u00d6\7?\2\2\u00d6\u00e3\7?\2\2\u00d7\u00d8\7#\2\2\u00d8\u00e3\7?\2\2"+
		"\u00d9\u00da\7@\2\2\u00da\u00e3\7?\2\2\u00db\u00dc\7>\2\2\u00dc\u00e3"+
		"\7?\2\2\u00dd\u00e3\t\4\2\2\u00de\u00df\7B\2\2\u00df\u00e3\7B\2\2\u00e0"+
		"\u00e1\7#\2\2\u00e1\u00e3\7B\2\2\u00e2\u00d5\3\2\2\2\u00e2\u00d7\3\2\2"+
		"\2\u00e2\u00d9\3\2\2\2\u00e2\u00db\3\2\2\2\u00e2\u00dd\3\2\2\2\u00e2\u00de"+
		"\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3$\3\2\2\2\u00e4\u00e5\7(\2\2\u00e5\u00e6"+
		"\7(\2\2\u00e6&\3\2\2\2\u00e7\u00e8\7~\2\2\u00e8\u00e9\7~\2\2\u00e9(\3"+
		"\2\2\2\u00ea\u00eb\7~\2\2\u00eb*\3\2\2\2\u00ec\u00ed\7#\2\2\u00ed,\3\2"+
		"\2\2\u00ee\u00ef\7*\2\2\u00ef.\3\2\2\2\u00f0\u00f1\7+\2\2\u00f1\60\3\2"+
		"\2\2\u00f2\u00f3\7}\2\2\u00f3\62\3\2\2\2\u00f4\u00f5\7\177\2\2\u00f5\64"+
		"\3\2\2\2\u00f6\u00f7\7)\2\2\u00f7\66\3\2\2\2\u00f8\u00f9\7$\2\2\u00f9"+
		"8\3\2\2\2\u00fa\u00fb\7\u0080\2\2\u00fb:\3\2\2\2\u00fc\u00fd\7@\2\2\u00fd"+
		"\u00fe\7@\2\2\u00fe<\3\2\2\2\u00ff\u0100\7]\2\2\u0100>\3\2\2\2\u0101\u0102"+
		"\7_\2\2\u0102@\3\2\2\2\u0103\u0104\7%\2\2\u0104B\3\2\2\2\u0105\u0106\7"+
		".\2\2\u0106D\3\2\2\2\u0107\u0108\7<\2\2\u0108F\3\2\2\2\u0109\u010a\7/"+
		"\2\2\u010aH\3\2\2\2\u010b\u010c\7\60\2\2\u010cJ\3\2\2\2\u010d\u010e\7"+
		"a\2\2\u010eL\3\2\2\2\u010f\u0110\7?\2\2\u0110N\3\2\2\2\u0111\u0112\7-"+
		"\2\2\u0112P\3\2\2\2\u0113\u0114\7A\2\2\u0114R\3\2\2\2\u0115\u0116\7,\2"+
		"\2\u0116T\3\2\2\2\u0117\u0118\7\61\2\2\u0118V\3\2\2\2\u0119\u011a\7&\2"+
		"\2\u011aX\3\2\2\2\u011b\u011c\7`\2\2\u011cZ\3\2\2\2\u011d\u011e\7v\2\2"+
		"\u011e\u011f\7t\2\2\u011f\u0120\7w\2\2\u0120\u0127\7g\2\2\u0121\u0122"+
		"\7h\2\2\u0122\u0123\7c\2\2\u0123\u0124\7n\2\2\u0124\u0125\7u\2\2\u0125"+
		"\u0127\7g\2\2\u0126\u011d\3\2\2\2\u0126\u0121\3\2\2\2\u0127\\\3\2\2\2"+
		"\u0128\u0131\7\62\2\2\u0129\u012d\t\5\2\2\u012a\u012c\t\6\2\2\u012b\u012a"+
		"\3\2\2\2\u012c\u012f\3\2\2\2\u012d\u012b\3\2\2\2\u012d\u012e\3\2\2\2\u012e"+
		"\u0131\3\2\2\2\u012f\u012d\3\2\2\2\u0130\u0128\3\2\2\2\u0130\u0129\3\2"+
		"\2\2\u0131^\3\2\2\2\u0132\u0134\5I%\2\u0133\u0135\t\7\2\2\u0134\u0133"+
		"\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u0134\3\2\2\2\u0136\u0137\3\2\2\2\u0137"+
		"`\3\2\2\2\u0138\u013b\5K&\2\u0139\u013b\t\b\2\2\u013a\u0138\3\2\2\2\u013a"+
		"\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013c\u013a\3\2\2\2\u013c\u013d\3\2"+
		"\2\2\u013d\u0144\3\2\2\2\u013e\u0143\t\t\2\2\u013f\u0143\5E#\2\u0140\u0143"+
		"\5G$\2\u0141\u0143\5K&\2\u0142\u013e\3\2\2\2\u0142\u013f\3\2\2\2\u0142"+
		"\u0140\3\2\2\2\u0142\u0141\3\2\2\2\u0143\u0146\3\2\2\2\u0144\u0142\3\2"+
		"\2\2\u0144\u0145\3\2\2\2\u0145b\3\2\2\2\u0146\u0144\3\2\2\2\u0147\u0149"+
		"\t\n\2\2\u0148\u0147\3\2\2\2\u0149\u014a\3\2\2\2\u014a\u0148\3\2\2\2\u014a"+
		"\u014b\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u014d\b\62\2\2\u014dd\3\2\2\2"+
		"\u014e\u014f\13\2\2\2\u014ff\3\2\2\2\17\2\u00c7\u00d0\u00e2\u0126\u012d"+
		"\u0130\u0136\u013a\u013c\u0142\u0144\u014a\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}