// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/common/makro/antlr4\NCMacroDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.common.makro.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCMacroDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LCURLY=1, RCURLY=2, LBR=3, RBR=4, VERT=5, COMMA=6, UNDERSCORE=7, INT=8, 
		TXT=9, WS=10, ErrorCharacter=11;
	public static final int
		RULE_line = 0, RULE_syn = 1, RULE_group = 2, RULE_minMax = 3, RULE_list = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"line", "syn", "group", "minMax", "list"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'['", "']'", "'|'", "','", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LCURLY", "RCURLY", "LBR", "RBR", "VERT", "COMMA", "UNDERSCORE", 
			"INT", "TXT", "WS", "ErrorCharacter"
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

	@Override
	public String getGrammarFileName() { return "NCMacroDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCMacroDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class LineContext extends ParserRuleContext {
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public LineContext line() {
			return getRuleContext(LineContext.class,0);
		}
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		return line(0);
	}

	private LineContext line(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LineContext _localctx = new LineContext(_ctx, _parentState);
		LineContext _prevctx = _localctx;
		int _startState = 0;
		enterRecursionRule(_localctx, 0, RULE_line, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(11);
			syn();
			}
			_ctx.stop = _input.LT(-1);
			setState(17);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LineContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_line);
					setState(13);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(14);
					syn();
					}
					} 
				}
				setState(19);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class SynContext extends ParserRuleContext {
		public TerminalNode TXT() { return getToken(NCMacroDslParser.TXT, 0); }
		public TerminalNode INT() { return getToken(NCMacroDslParser.INT, 0); }
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public SynContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_syn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterSyn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitSyn(this);
		}
	}

	public final SynContext syn() throws RecognitionException {
		SynContext _localctx = new SynContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_syn);
		int _la;
		try {
			setState(22);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT:
			case TXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(20);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==TXT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case LCURLY:
				enterOuterAlt(_localctx, 2);
				{
				setState(21);
				group();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class GroupContext extends ParserRuleContext {
		public TerminalNode LCURLY() { return getToken(NCMacroDslParser.LCURLY, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode RCURLY() { return getToken(NCMacroDslParser.RCURLY, 0); }
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitGroup(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_group);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			match(LCURLY);
			setState(25);
			list(0);
			setState(26);
			match(RCURLY);
			setState(28);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(27);
				minMax();
				}
				break;
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

	public static class MinMaxContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCMacroDslParser.LBR, 0); }
		public List<TerminalNode> INT() { return getTokens(NCMacroDslParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(NCMacroDslParser.INT, i);
		}
		public TerminalNode COMMA() { return getToken(NCMacroDslParser.COMMA, 0); }
		public TerminalNode RBR() { return getToken(NCMacroDslParser.RBR, 0); }
		public MinMaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMax; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_minMax);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(LBR);
			setState(31);
			match(INT);
			setState(32);
			match(COMMA);
			setState(33);
			match(INT);
			setState(34);
			match(RBR);
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

	public static class ListContext extends ParserRuleContext {
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode VERT() { return getToken(NCMacroDslParser.VERT, 0); }
		public TerminalNode UNDERSCORE() { return getToken(NCMacroDslParser.UNDERSCORE, 0); }
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitList(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		return list(0);
	}

	private ListContext list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ListContext _localctx = new ListContext(_ctx, _parentState);
		ListContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(37);
			syn();
			}
			_ctx.stop = _input.LT(-1);
			setState(47);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_list);
					setState(39);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(40);
					match(VERT);
					setState(43);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case LCURLY:
					case INT:
					case TXT:
						{
						setState(41);
						syn();
						}
						break;
					case UNDERSCORE:
						{
						setState(42);
						match(UNDERSCORE);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					} 
				}
				setState(49);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 0:
			return line_sempred((LineContext)_localctx, predIndex);
		case 4:
			return list_sempred((ListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean line_sempred(LineContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean list_sempred(ListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\r\65\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\3\2\3\2\7\2\22\n\2\f\2\16\2\25"+
		"\13\2\3\3\3\3\5\3\31\n\3\3\4\3\4\3\4\3\4\5\4\37\n\4\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6.\n\6\7\6\60\n\6\f\6\16\6\63\13"+
		"\6\3\6\2\4\2\n\7\2\4\6\b\n\2\3\3\2\n\13\2\64\2\f\3\2\2\2\4\30\3\2\2\2"+
		"\6\32\3\2\2\2\b \3\2\2\2\n&\3\2\2\2\f\r\b\2\1\2\r\16\5\4\3\2\16\23\3\2"+
		"\2\2\17\20\f\3\2\2\20\22\5\4\3\2\21\17\3\2\2\2\22\25\3\2\2\2\23\21\3\2"+
		"\2\2\23\24\3\2\2\2\24\3\3\2\2\2\25\23\3\2\2\2\26\31\t\2\2\2\27\31\5\6"+
		"\4\2\30\26\3\2\2\2\30\27\3\2\2\2\31\5\3\2\2\2\32\33\7\3\2\2\33\34\5\n"+
		"\6\2\34\36\7\4\2\2\35\37\5\b\5\2\36\35\3\2\2\2\36\37\3\2\2\2\37\7\3\2"+
		"\2\2 !\7\5\2\2!\"\7\n\2\2\"#\7\b\2\2#$\7\n\2\2$%\7\6\2\2%\t\3\2\2\2&\'"+
		"\b\6\1\2\'(\5\4\3\2(\61\3\2\2\2)*\f\3\2\2*-\7\7\2\2+.\5\4\3\2,.\7\t\2"+
		"\2-+\3\2\2\2-,\3\2\2\2.\60\3\2\2\2/)\3\2\2\2\60\63\3\2\2\2\61/\3\2\2\2"+
		"\61\62\3\2\2\2\62\13\3\2\2\2\63\61\3\2\2\2\7\23\30\36-\61";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}