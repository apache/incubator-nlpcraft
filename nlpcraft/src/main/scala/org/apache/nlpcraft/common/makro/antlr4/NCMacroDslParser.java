// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/common/makro/antlr4/NCMacroDsl.g4 by ANTLR 4.9.1
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
		RULE_makro = 0, RULE_expr = 1, RULE_item = 2, RULE_syn = 3, RULE_group = 4, 
		RULE_minMax = 5, RULE_list = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"makro", "expr", "item", "syn", "group", "minMax", "list"
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

	public static class MakroContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public MakroContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_makro; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterMakro(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitMakro(this);
		}
	}

	public final MakroContext makro() throws RecognitionException {
		MakroContext _localctx = new MakroContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_makro);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			expr(0);
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

	public static class ExprContext extends ParserRuleContext {
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(17);
			item();
			}
			_ctx.stop = _input.LT(-1);
			setState(23);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(19);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(20);
					item();
					}
					} 
				}
				setState(25);
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

	public static class ItemContext extends ParserRuleContext {
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitItem(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_item);
		try {
			setState(28);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT:
			case TXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(26);
				syn();
				}
				break;
			case LCURLY:
				enterOuterAlt(_localctx, 2);
				{
				setState(27);
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

	public static class SynContext extends ParserRuleContext {
		public TerminalNode TXT() { return getToken(NCMacroDslParser.TXT, 0); }
		public TerminalNode INT() { return getToken(NCMacroDslParser.INT, 0); }
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
		enterRule(_localctx, 6, RULE_syn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
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
		enterRule(_localctx, 8, RULE_group);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			match(LCURLY);
			setState(33);
			list(0);
			setState(34);
			match(RCURLY);
			setState(36);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(35);
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
		enterRule(_localctx, 10, RULE_minMax);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			match(LBR);
			setState(39);
			match(INT);
			setState(40);
			match(COMMA);
			setState(41);
			match(INT);
			setState(42);
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
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
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
		int _startState = 12;
		enterRecursionRule(_localctx, 12, RULE_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(45);
			expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(55);
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
					setState(47);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(48);
					match(VERT);
					setState(51);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case LCURLY:
					case INT:
					case TXT:
						{
						setState(49);
						expr(0);
						}
						break;
					case UNDERSCORE:
						{
						setState(50);
						match(UNDERSCORE);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					} 
				}
				setState(57);
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
		case 1:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 6:
			return list_sempred((ListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\r=\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\3\3\3\3\3\3\3\3\3"+
		"\7\3\30\n\3\f\3\16\3\33\13\3\3\4\3\4\5\4\37\n\4\3\5\3\5\3\6\3\6\3\6\3"+
		"\6\5\6\'\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\66"+
		"\n\b\7\b8\n\b\f\b\16\b;\13\b\3\b\2\4\4\16\t\2\4\6\b\n\f\16\2\3\3\2\n\13"+
		"\2:\2\20\3\2\2\2\4\22\3\2\2\2\6\36\3\2\2\2\b \3\2\2\2\n\"\3\2\2\2\f(\3"+
		"\2\2\2\16.\3\2\2\2\20\21\5\4\3\2\21\3\3\2\2\2\22\23\b\3\1\2\23\24\5\6"+
		"\4\2\24\31\3\2\2\2\25\26\f\3\2\2\26\30\5\6\4\2\27\25\3\2\2\2\30\33\3\2"+
		"\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\5\3\2\2\2\33\31\3\2\2\2\34\37\5\b"+
		"\5\2\35\37\5\n\6\2\36\34\3\2\2\2\36\35\3\2\2\2\37\7\3\2\2\2 !\t\2\2\2"+
		"!\t\3\2\2\2\"#\7\3\2\2#$\5\16\b\2$&\7\4\2\2%\'\5\f\7\2&%\3\2\2\2&\'\3"+
		"\2\2\2\'\13\3\2\2\2()\7\5\2\2)*\7\n\2\2*+\7\b\2\2+,\7\n\2\2,-\7\6\2\2"+
		"-\r\3\2\2\2./\b\b\1\2/\60\5\4\3\2\609\3\2\2\2\61\62\f\3\2\2\62\65\7\7"+
		"\2\2\63\66\5\4\3\2\64\66\7\t\2\2\65\63\3\2\2\2\65\64\3\2\2\2\668\3\2\2"+
		"\2\67\61\3\2\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:\17\3\2\2\2;9\3\2\2\2"+
		"\7\31\36&\659";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}