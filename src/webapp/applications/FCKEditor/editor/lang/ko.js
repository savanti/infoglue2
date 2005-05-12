/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2005 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * File Name: ko.js
 * 	Korean language file.
 * 
 * File Authors:
 * 		Taehwan Kwag (thkwag@nate.com)
 */

var FCKLang =
{
// Language direction : "ltr" (left to right) or "rtl" (right to left).
Dir					: "ltr",

ToolbarCollapse		: "ํด๋ฐ ๊ฐ?์ถ๊ธฐ",
ToolbarExpand		: "ํด๋ฐ ๋ณด์?ด๊ธฐ",

// Toolbar Items and Context Menu
Save				: "์ ์ฅํ๊ธฐ",
NewPage				: "์ ๋ฌธ์",
Preview				: "๋ฏธ๋ฆฌ๋ณด๊ธฐ",
Cut					: "์๋?ผ๋ด๊ธฐ",
Copy				: "๋ณต์ฌํ๊ธฐ",
Paste				: "๋ถ์ฌ๋ฃ๊ธฐ",
PasteText			: "ํ?์คํธ๋ก ๋ถ์ฌ๋ฃ๊ธฐ",
PasteWord			: "MS Word ํ์?์?์ ๋ถ์ฌ๋ฃ๊ธฐ",
Print				: "์?ธ์ํ๊ธฐ",
SelectAll			: "์ ์ฒด์ ํ?",
RemoveFormat		: "ํ?ฌ๋งท ์ง์ฐ๊ธฐ",
InsertLinkLbl		: "๋ง?ํ?ฌ",
InsertLink			: "๋ง?ํ?ฌ ์ฝ์/๋ณ๊ฒฝ",
RemoveLink			: "๋ง?ํ?ฌ ์ญ์ ",
Anchor				: "์ฑ๊ฐํผ ์ฝ์/๋ณ๊ฒฝ",
InsertImageLbl		: "์?ด๋ฏธ์ง",
InsertImage			: "์?ด๋ฏธ์ง ์ฝ์/๋ณ๊ฒฝ",
InsertTableLbl		: "ํ",
InsertTable			: "ํ ์ฝ์/๋ณ๊ฒฝ",
InsertLineLbl		: "์ํ?์ ",
InsertLine			: "์ํ?์  ์ฝ์",
InsertSpecialCharLbl: "ํน์๋ฌธ์? ์ฝ์",
InsertSpecialChar	: "ํน์๋ฌธ์? ์ฝ์",
InsertSmileyLbl		: "์์?ด์ฝ",
InsertSmiley		: "์์?ด์ฝ ์ฝ์",
About				: "FCKeditor์? ๋ํ์ฌ",
Bold				: "์งํ๊ฒ",
Italic				: "์?ดํ๋ฆญ",
Underline			: "๋ฐ์ค",
StrikeThrough		: "์ทจ์์ ",
Subscript			: "์๋ ์ฒจ์?",
Superscript			: "์ ์ฒจ์?",
LeftJustify			: "์ผ์ชฝ ์ ๋ ฌ",
CenterJustify		: "๊ฐ์ด๋?ฐ ์ ๋ ฌ",
RightJustify		: "์ค๋ฅธ์ชฝ ์ ๋ ฌ",
BlockJustify		: "์์ชฝ ๋ง์ถค",
DecreaseIndent		: "๋ด์ด์ฐ๊ธฐ",
IncreaseIndent		: "๋ค์ฌ์ฐ๊ธฐ",
Undo				: "์ทจ์",
Redo				: "์ฌ์คํ",
NumberedListLbl		: "์์์๋ ๋ชฉ๋ก?",
NumberedList		: "์์์๋ ๋ชฉ๋ก?",
BulletedListLbl		: "์์์๋ ๋ชฉ๋ก?",
BulletedList		: "์์์๋ ๋ชฉ๋ก?",
ShowTableBorders	: "ํ ํ๋?๋ฆฌ ๋ณด๊ธฐ",
ShowDetails			: "๋ฌธ์๊ธฐํธ ๋ณด๊ธฐ",
Style				: "์คํ์?ผ",
FontFormat			: "ํ?ฌ๋งท",
Font				: "ํ?ฐํธ",
FontSize			: "๊ธ์? ํ?ฌ๊ธฐ",
TextColor			: "๊ธ์? ์์?",
BGColor				: "๋ฐฐ๊ฒฝ ์์?",
Source				: "์์ค",
Find				: "์ฐพ๊ธฐ",
Replace				: "๋ฐ๊พธ๊ธฐ",
SpellCheck			: "์ฒ ์?๊ฒ์ฌ",
UniversalKeyboard	: "๋ค๊ตญ์ด ์๋ ฅ๊ธฐ",

Form			: "ํ?ผ",
Checkbox		: "์ฒดํ?ฌ๋ฐ์ค",
RadioButton		: "๋?ผ๋์ค๋ฒํผ",
TextField		: "์๋ ฅํ๋",
Textarea		: "์๋ ฅ์?์ญ",
HiddenField		: "์จ๊นํ๋",
Button			: "๋ฒํผ",
SelectionField	: "ํผ์นจ๋ชฉ๋ก?",
ImageButton		: "์?ด๋ฏธ์ง๋ฒํผ",

// Context Menu
EditLink			: "๋ง?ํ?ฌ ์์ ",
InsertRow			: "๊ฐ๋ก์ค ์ฝ์",
DeleteRows			: "๊ฐ๋ก์ค ์ญ์ ",
InsertColumn		: "์ธ๋ก์ค ์ฝ์",
DeleteColumns		: "์ธ๋ก์ค ์ญ์ ",
InsertCell			: "์ ์ฝ์",
DeleteCells			: "์ ์ญ์ ",
MergeCells			: "์ ํฉ์น๊ธฐ",
SplitCell			: "์ ๋๋๊ธฐ",
CellProperties		: "์ ์?์ฑ",
TableProperties		: "ํ ์?์ฑ",
ImageProperties		: "์?ด๋ฏธ์ง ์?์ฑ",

AnchorProp			: "์ฑ๊ฐํผ ์?์ฑ",
ButtonProp			: "๋ฒํผ ์?์ฑ",
CheckboxProp		: "์ฒดํ?ฌ๋ฐ์ค ์?์ฑ",
HiddenFieldProp		: "์จ๊นํ๋ ์?์ฑ",
RadioButtonProp		: "๋?ผ๋์ค๋ฒํผ ์?์ฑ",
ImageButtonProp		: "์?ด๋ฏธ์ง๋ฒํผ ์?์ฑ",
TextFieldProp		: "์๋ ฅํ๋ ์?์ฑ",
SelectionFieldProp	: "ํผ์นจ๋ชฉ๋ก? ์?์ฑ",
TextareaProp		: "์๋ ฅ์?์ญ ์?์ฑ",
FormProp			: "ํ?ผ ์?์ฑ",

FontFormats			: "Normal;Formatted;Address;Heading 1;Heading 2;Heading 3;Heading 4;Heading 5;Heading 6",

// Alerts and Messages
ProcessingXHTML		: "XHTML ์ฒ๋ฆฌ์ค. ์ ์๋ง ๊ธฐ๋ค๋ ค์ฃผ์ญ์์.",
Done				: "์๋ฃ",
PasteWordConfirm	: "๋ถ์ฌ๋ฃ๊ธฐ ํ  ํ?์คํธ๋ MS Word์?์ ๋ณต์ฌํ ๊ฒ์๋๋ค. ๋ถ์ฌ๋ฃ๊ธฐ ์ ์? MS Word ํ?ฌ๋ฉง์? ์ญ์ ํ์๊ฒ ์ต๋๊น?",
NotCompatiblePaste	: "์?ด ๋ช๋ น์? ์?ธํฐ๋ท์?ต์คํ๋ก๋ฌ 5.5 ๋ฒ์  ์?ด์?์?์๋ง ์๋?ํฉ๋๋ค. ํ?ฌ๋ฉง์? ์ญ์ ํ์ง ์๊ณ  ๋ถ์ฌ๋ฃ๊ธฐ ํ์๊ฒ ์ต๋๊น?",
UnknownToolbarItem	: "์์์๋ ํด๋ฐ์๋๋ค. : \"%1\"",
UnknownCommand		: "์์์๋ ๊ธฐ๋ฅ์๋๋ค. : \"%1\"",
NotImplemented		: "๊ธฐ๋ฅ์?ด ์คํ๋?์ง ์์์ต๋๋ค.",
UnknownToolbarSet	: "ํด๋ฐ ์ค์ ์?ด ์์ต๋๋ค. : \"%1\"",

// Dialogs
DlgBtnOK			: "์",
DlgBtnCancel		: "์๋์ค",
DlgBtnClose			: "๋ซ๊ธฐ",
DlgBtnBrowseServer	: "์๋ฒ ๋ณด๊ธฐ",
DlgAdvancedTag		: "์?์ธํ",
DlgOpOther			: "&lt;๊ธฐํ&gt;",

// General Dialogs Labels
DlgGenNotSet		: "&lt;์ค์ ๋?์ง ์์?&gt;",
DlgGenId			: "ID",
DlgGenLangDir		: "์ฐ๊ธฐ ๋ฐฉํฅ",
DlgGenLangDirLtr	: "์ผ์ชฝ์?์ ์ค๋ฅธ์ชฝ (LTR)",
DlgGenLangDirRtl	: "์ค๋ฅธ์ชฝ์?์ ์ผ์ชฝ (RTL)",
DlgGenLangCode		: "์ธ์ด ์ฝ๋",
DlgGenAccessKey		: "์์ธ์ค ํค",
DlgGenName			: "Name",
DlgGenTabIndex		: "ํญ ์์",
DlgGenLongDescr		: "URL ์ค๋ช",
DlgGenClass			: "Stylesheet Classes",
DlgGenTitle			: "Advisory Title",
DlgGenContType		: "Advisory Content Type",
DlgGenLinkCharset	: "Linked Resource Charset",
DlgGenStyle			: "Style",

// Image Dialog
DlgImgTitle			: "์?ด๋ฏธ์ง ์ค์ ",
DlgImgInfoTab		: "์?ด๋ฏธ์ง ์ ๋ณด",
DlgImgBtnUpload		: "์๋ฒ๋ก ์ ์ก",
DlgImgURL			: "URL",
DlgImgUpload		: "์๋ก๋",
DlgImgAlt			: "์?ด๋ฏธ์ง ์ค๋ช",
DlgImgWidth			: "๋๋น",
DlgImgHeight		: "๋์?ด",
DlgImgLockRatio		: "๋น์จ ์ ์ง",
DlgBtnResetSize		: "์?๋ ํ?ฌ๊ธฐ๋ก",
DlgImgBorder		: "ํ๋?๋ฆฌ",
DlgImgHSpace		: "์ํ?์ฌ๋ฐฑ",
DlgImgVSpace		: "์์ง?์ฌ๋ฐฑ",
DlgImgAlign			: "์ ๋ ฌ",
DlgImgAlignLeft		: "์ผ์ชฝ",
DlgImgAlignAbsBottom: "์ค์๋(Abs Bottom)",
DlgImgAlignAbsMiddle: "์ค์ค๊ฐ(Abs Middle)",
DlgImgAlignBaseline	: "๊ธฐ์ค์ ",
DlgImgAlignBottom	: "์๋",
DlgImgAlignMiddle	: "์ค๊ฐ",
DlgImgAlignRight	: "์ค๋ฅธ์ชฝ",
DlgImgAlignTextTop	: "๊ธ์?์(Text Top)",
DlgImgAlignTop		: "์",
DlgImgPreview		: "๋ฏธ๋ฆฌ๋ณด๊ธฐ",
DlgImgAlertUrl		: "์?ด๋ฏธ์ง URL์? ์๋ ฅํ์ญ์์",
DlgImgLinkTab		: "๋ง?ํ?ฌ",

// Link Dialog
DlgLnkWindowTitle	: "๋ง?ํ?ฌ",
DlgLnkInfoTab		: "๋ง?ํ?ฌ ์ ๋ณด",
DlgLnkTargetTab		: "ํ๊ฒ",

DlgLnkType			: "๋ง?ํ?ฌ ์ข๋ฅ",
DlgLnkTypeURL		: "URL",
DlgLnkTypeAnchor	: "์ฑ๊ฐํผ",
DlgLnkTypeEMail		: "์?ด๋ฉ์?ผ",
DlgLnkProto			: "ํ๋กํ ์ฝ",
DlgLnkProtoOther	: "&lt;๊ธฐํ&gt;",
DlgLnkURL			: "URL",
DlgLnkAnchorSel		: "์ฑ๊ฐํผ ์ ํ?",
DlgLnkAnchorByName	: "์ฑ๊ฐํผ ์?ด๋ฆ",
DlgLnkAnchorById	: "์ฑ๊ฐํผ ID",
DlgLnkNoAnchors		: "&lt;๋ฌธ์์? ์ฑ๊ฐํผ๊ฐ ์์ต๋๋ค.&gt;",
DlgLnkEMail			: "์?ด๋ฉ์?ผ ์ฃผ์",
DlgLnkEMailSubject	: "์ ๋ชฉ",
DlgLnkEMailBody		: "๋ด์ฉ",
DlgLnkUpload		: "์๋ก๋",
DlgLnkBtnUpload		: "์๋ฒ๋ก ์ ์ก",

DlgLnkTarget		: "ํ๊ฒ",
DlgLnkTargetFrame	: "&lt;ํ๋ ์&gt;",
DlgLnkTargetPopup	: "&lt;ํ?์์ฐฝ&gt;",
DlgLnkTargetBlank	: "์ ์ฐฝ (_blank)",
DlgLnkTargetParent	: "๋ถ๋ชจ ์ฐฝ (_parent)",
DlgLnkTargetSelf	: "ํ์ฌ ์ฐฝ (_self)",
DlgLnkTargetTop		: "์ต ์?์ ์ฐฝ (_top)",
DlgLnkTargetFrameName	: "ํ๊ฒ ํ๋ ์ ์?ด๋ฆ",
DlgLnkPopWinName	: "ํ?์์ฐฝ ์?ด๋ฆ",
DlgLnkPopWinFeat	: "ํ?์์ฐฝ ์ค์ ",
DlgLnkPopResize		: "ํ?ฌ๊ธฐ์กฐ์ ",
DlgLnkPopLocation	: "์ฃผ์ํ์์ค",
DlgLnkPopMenu		: "๋ฉ๋ด๋ฐ",
DlgLnkPopScroll		: "์คํ?ฌ๋กค๋ฐ",
DlgLnkPopStatus		: "์?ํ๋ฐ",
DlgLnkPopToolbar	: "ํด๋ฐ",
DlgLnkPopFullScrn	: "์ ์ฒดํ๋ฉด (IE)",
DlgLnkPopDependent	: "Dependent (Netscape)",
DlgLnkPopWidth		: "๋๋น",
DlgLnkPopHeight		: "๋์?ด",
DlgLnkPopLeft		: "์ผ์ชฝ ์์น",
DlgLnkPopTop		: "์์ชฝ ์์น",

DlnLnkMsgNoUrl		: "๋ง?ํ?ฌ URL์? ์๋ ฅํ์ญ์์.",
DlnLnkMsgNoEMail	: "์?ด๋ฉ์?ผ์ฃผ์๋ฅผ ์๋ ฅํ์ญ์์.",
DlnLnkMsgNoAnchor	: "์ฑ๊ฐํผ๋ช์? ์๋ ฅํ์ญ์์.",

// Color Dialog
DlgColorTitle		: "์์? ์ ํ?",
DlgColorBtnClear	: "์ง์ฐ๊ธฐ",
DlgColorHighlight	: "ํ์ฌ",
DlgColorSelected	: "์ ํ?๋?จ",

// Smiley Dialog
DlgSmileyTitle		: "์์?ด์ฝ ์ฝ์",

// Special Character Dialog
DlgSpecialCharTitle	: "ํน์๋ฌธ์? ์ ํ?",

// Table Dialog
DlgTableTitle		: "ํ ์ค์ ",
DlgTableRows		: "๊ฐ๋ก์ค",
DlgTableColumns		: "์ธ๋ก์ค",
DlgTableBorder		: "ํ๋?๋ฆฌ ํ?ฌ๊ธฐ",
DlgTableAlign		: "์ ๋ ฌ",
DlgTableAlignNotSet	: "<์ค์ ๋?์ง ์์?>",
DlgTableAlignLeft	: "์ผ์ชฝ",
DlgTableAlignCenter	: "๊ฐ์ด๋?ฐ",
DlgTableAlignRight	: "์ค๋ฅธ์ชฝ",
DlgTableWidth		: "๋๋น",
DlgTableWidthPx		: "ํฝ์",
DlgTableWidthPc		: "ํ?ผ์ผํธ",
DlgTableHeight		: "๋์?ด",
DlgTableCellSpace	: "์ ๊ฐ๊ฒฉ",
DlgTableCellPad		: "์ ์ฌ๋ฐฑ",
DlgTableCaption		: "์บก์",

// Table Cell Dialog
DlgCellTitle		: "์ ์ค์ ",
DlgCellWidth		: "๋๋น",
DlgCellWidthPx		: "ํฝ์",
DlgCellWidthPc		: "ํ?ผ์ผํธ",
DlgCellHeight		: "๋์?ด",
DlgCellWordWrap		: "์๋๋ฉ",
DlgCellWordWrapNotSet	: "<์ค์ ๋?์ง ์์?>",
DlgCellWordWrapYes	: "์",
DlgCellWordWrapNo	: "์๋์ค",
DlgCellHorAlign		: "์ํ? ์ ๋ ฌ",
DlgCellHorAlignNotSet	: "<์ค์ ๋?์ง ์์?>",
DlgCellHorAlignLeft	: "์ผ์ชฝ",
DlgCellHorAlignCenter	: "๊ฐ์ด๋?ฐ",
DlgCellHorAlignRight: "์ค๋ฅธ์ชฝ",
DlgCellVerAlign		: "์์ง? ์ ๋ ฌ",
DlgCellVerAlignNotSet	: "<์ค์ ๋?์ง ์์?>",
DlgCellVerAlignTop	: "์",
DlgCellVerAlignMiddle	: "์ค๊ฐ",
DlgCellVerAlignBottom	: "์๋",
DlgCellVerAlignBaseline	: "๊ธฐ์ค์ ",
DlgCellRowSpan		: "์ธ๋ก ํฉ์น๊ธฐ",
DlgCellCollSpan		: "๊ฐ๋ก ํฉ์น๊ธฐ",
DlgCellBackColor	: "๋ฐฐ๊ฒฝ ์์?",
DlgCellBorderColor	: "ํ๋?๋ฆฌ ์์?",
DlgCellBtnSelect	: "์ ํ?",

// Find Dialog
DlgFindTitle		: "์ฐพ๊ธฐ",
DlgFindFindBtn		: "์ฐพ๊ธฐ",
DlgFindNotFoundMsg	: "๋ฌธ์?์ด์? ์ฐพ์? ์ ์์ต๋๋ค.",

// Replace Dialog
DlgReplaceTitle			: "๋ฐ๊พธ๊ธฐ",
DlgReplaceFindLbl		: "์ฐพ์? ๋ฌธ์?์ด:",
DlgReplaceReplaceLbl	: "๋ฐ๊ฟ ๋ฌธ์?์ด:",
DlgReplaceCaseChk		: "๋์๋ฌธ์? ๊ตฌ๋ถ",
DlgReplaceReplaceBtn	: "๋ฐ๊พธ๊ธฐ",
DlgReplaceReplAllBtn	: "๋ชจ๋? ๋ฐ๊พธ๊ธฐ",
DlgReplaceWordChk		: "์จ์ ํ ๋จ์ด",

// Paste Operations / Dialog
PasteErrorPaste	: "๋ธ๋?ผ์ฐ์ ์? ๋ณด์์ค์ ๋๋ฌธ์? ๋ถ์ฌ๋ฃ๊ธฐ ๊ธฐ๋ฅ์? ์คํํ  ์ ์์ต๋๋ค. ํค๋ณด๋ ๋ช๋ น์? ์ฌ์ฉํ์ญ์์. (Ctrl+V).",
PasteErrorCut	: "๋ธ๋?ผ์ฐ์ ์? ๋ณด์์ค์ ๋๋ฌธ์? ์๋?ผ๋ด๊ธฐ ๊ธฐ๋ฅ์? ์คํํ  ์ ์์ต๋๋ค. ํค๋ณด๋ ๋ช๋ น์? ์ฌ์ฉํ์ญ์์. (Ctrl+X).",
PasteErrorCopy	: "๋ธ๋?ผ์ฐ์ ์? ๋ณด์์ค์ ๋๋ฌธ์? ๋ณต์ฌํ๊ธฐ ๊ธฐ๋ฅ์? ์คํํ  ์ ์์ต๋๋ค. ํค๋ณด๋ ๋ช๋ น์? ์ฌ์ฉํ์ญ์์.  (Ctrl+C).",

PasteAsText		: "ํ?์คํธ๋ก ๋ถ์ฌ๋ฃ๊ธฐ",
PasteFromWord	: "MS Word ํ์?์?์ ๋ถ์ฌ๋ฃ๊ธฐ",

DlgPasteMsg		: "๋ธ๋?ผ์ฐ์ ์? <STRONG>๋ณด์์ค์ /STRONG> ๋๋ฌธ์? ๋ถ์ฌ๋ฃ๊ธฐ ํ  ์ ์์ต๋๋ค. <BR>ํค๋ณด๋ ๋ช๋ น(<STRONG>Ctrl+V</STRONG>)์? ์?ด์ฉํ์ฌ ๋ถ์ฌ๋ฃ์? ๋ค์? <STRONG>์</STRONG>๋ฒํผ์? ํ?ด๋ฆญํ์ญ์์.",

// Color Picker
ColorAutomatic	: "๊ธฐ๋ณธ์์?",
ColorMoreColors	: "์์?์ ํ?...",

// Document Properties
DocProps		: "๋ฌธ์ ์?์ฑ",

// Anchor Dialog
DlgAnchorTitle		: "์ฑ๊ฐํผ ์?์ฑ",
DlgAnchorName		: "์ฑ๊ฐํผ ์?ด๋ฆ",
DlgAnchorErrorName	: "์ฑ๊ฐํผ ์?ด๋ฆ์? ์๋ ฅํ์ญ์์.",

// Speller Pages Dialog
DlgSpellNotInDic		: "์ฌ์ ์? ์๋ ๋จ์ด",
DlgSpellChangeTo		: "๋ณ๊ฒฝํ  ๋จ์ด",
DlgSpellBtnIgnore		: "๊ฑด๋๋",
DlgSpellBtnIgnoreAll	: "๋ชจ๋? ๊ฑด๋๋",
DlgSpellBtnReplace		: "๋ณ๊ฒฝ",
DlgSpellBtnReplaceAll	: "๋ชจ๋? ๋ณ๊ฒฝ",
DlgSpellBtnUndo			: "์ทจ์",
DlgSpellNoSuggestions	: "- ์ถ์ฒ๋จ์ด ์์? -",
DlgSpellProgress		: "์ฒ ์?๊ฒ์ฌ๋ฅผ ์งํ์ค์๋๋ค...",
DlgSpellNoMispell		: "์ฒ ์?๊ฒ์ฌ ์๋ฃ: ์๋ชป๋? ์ฒ ์?๊ฐ ์์ต๋๋ค.",
DlgSpellNoChanges		: "์ฒ ์?๊ฒ์ฌ ์๋ฃ: ๋ณ๊ฒฝ๋? ๋จ์ด๊ฐ ์์ต๋๋ค.",
DlgSpellOneChange		: "์ฒ ์?๊ฒ์ฌ ์๋ฃ: ๋จ์ด๊ฐ ๋ณ๊ฒฝ๋?์์ต๋๋ค.",
DlgSpellManyChanges		: "์ฒ ์?๊ฒ์ฌ ์๋ฃ: %1 ๋จ์ด๊ฐ ๋ณ๊ฒฝ๋?์์ต๋๋ค.",

IeSpellDownload			: "์ฒ ์? ๊ฒ์ฌ๊ธฐ๊ฐ ์ฒ ์น๋?์ง ์์์ต๋๋ค. ์ง๊ธ ๋ค์ด๋ก๋ํ์๊ฒ ์ต๋๊น?",

// Button Dialog
DlgButtonText	: "๋ฒํผ๊ธ์?(๊ฐ)",
DlgButtonType	: "๋ฒํผ์ข๋ฅ",

// Checkbox and Radio Button Dialogs
DlgCheckboxName		: "์?ด๋ฆ",
DlgCheckboxValue	: "๊ฐ",
DlgCheckboxSelected	: "์ ํ?๋?จ",

// Form Dialog
DlgFormName		: "ํ?ผ์?ด๋ฆ",
DlgFormAction	: "์คํ๊ฒฝ๋ก(Action)",
DlgFormMethod	: "๋ฐฉ๋ฒ(Method)",

// Select Field Dialog
DlgSelectName		: "์?ด๋ฆ",
DlgSelectValue		: "๊ฐ",
DlgSelectSize		: "์ธ๋กํ?ฌ๊ธฐ",
DlgSelectLines		: "์ค",
DlgSelectChkMulti	: "์ฌ๋ฌํญ๋ชฉ ์ ํ? ํ์ฉ",
DlgSelectOpAvail	: "์ ํ?์ต์",
DlgSelectOpText		: "์?ด๋ฆ",
DlgSelectOpValue	: "๊ฐ",
DlgSelectBtnAdd		: "์ถ๊ฐ",
DlgSelectBtnModify	: "๋ณ๊ฒฝ",
DlgSelectBtnUp		: "์๋ก",
DlgSelectBtnDown	: "์๋๋ก",
DlgSelectBtnSetValue : "์ ํ?๋?๊ฒ์ผ๋ก ์ค์ ",
DlgSelectBtnDelete	: "์ญ์ ",

// Textarea Dialog
DlgTextareaName	: "์?ด๋ฆ",
DlgTextareaCols	: "์นธ์",
DlgTextareaRows	: "์ค์",

// Text Field Dialog
DlgTextName			: "์?ด๋ฆ",
DlgTextValue		: "๊ฐ",
DlgTextCharWidth	: "๊ธ์? ๋๋น",
DlgTextMaxChars		: "์ต๋ ๊ธ์?์",
DlgTextType			: "์ข๋ฅ",
DlgTextTypeText		: "๋ฌธ์?์ด",
DlgTextTypePass		: "๋น๋ฐ๋ฒํธ",

// Hidden Field Dialog
DlgHiddenName	: "์?ด๋ฆ",
DlgHiddenValue	: "๊ฐ",

// Bulleted List Dialog
BulletedListProp	: "์์์๋ ๋ชฉ๋ก? ์?์ฑ",
NumberedListProp	: "์์์๋ ๋ชฉ๋ก? ์?์ฑ",
DlgLstType			: "์ข๋ฅ",
DlgLstTypeCircle	: "์?(Circle)",
DlgLstTypeDisk		: "๋ฅ๊ทผ์ ?(Disk)",
DlgLstTypeSquare	: "๋ค๋ชจ์ ?(Square)",
DlgLstTypeNumbers	: "๋ฒํธ (1, 2, 3)",
DlgLstTypeLCase		: "์๋ฌธ์? (a, b, c)",
DlgLstTypeUCase		: "๋๋ฌธ์? (A, B, C)",
DlgLstTypeSRoman	: "๋ก๋ง์? ์๋ฌธ์? (i, ii, iii)",
DlgLstTypeLRoman	: "๋ก๋ง์? ๋๋ฌธ์? (I, II, III)",

// Document Properties Dialog
DlgDocGeneralTab	: "์?ผ๋ฐ",
DlgDocBackTab		: "๋ฐฐ๊ฒฝ",
DlgDocColorsTab		: "์์? ๋ฐ? ์ฌ๋ฐฑ",
DlgDocMetaTab		: "๋ฉํ๋?ฐ์?ดํฐ",

DlgDocPageTitle		: "ํ์?ด์ง๋ช",
DlgDocLangDir		: "๋ฌธ์? ์ฐ๊ธฐ๋ฐฉํฅ",
DlgDocLangDirLTR	: "์ผ์ชฝ์?์ ์ค๋ฅธ์ชฝ (LTR)",
DlgDocLangDirRTL	: "์ค๋ฅธ์ชฝ์?์ ์ผ์ชฝ (RTL)",
DlgDocLangCode		: "์ธ์ด์ฝ๋",
DlgDocCharSet		: "์บ?๋ฆญํฐ์ ์?ธ์ฝ๋ฉ",
DlgDocCharSetOther	: "๋ค๋ฅธ ์บ?๋ฆญํฐ์ ์?ธ์ฝ๋ฉ",

DlgDocDocType		: "๋ฌธ์ ํค๋",
DlgDocDocTypeOther	: "๋ค๋ฅธ ๋ฌธ์ํค๋",
DlgDocIncXHTML		: "XHTML ๋ฌธ์์ ์? ํ?ฌํจ",
DlgDocBgColor		: "๋ฐฐ๊ฒฝ์์?",
DlgDocBgImage		: "๋ฐฐ๊ฒฝ์?ด๋ฏธ์ง URL",
DlgDocBgNoScroll	: "์คํ?ฌ๋กค๋?์ง์๋ ๋ฐฐ๊ฒฝ",
DlgDocCText			: "ํ?์คํธ",
DlgDocCLink			: "๋ง?ํ?ฌ",
DlgDocCVisited		: "๋ฐฉ๋ฌธํ ๋ง?ํ?ฌ(Visited)",
DlgDocCActive		: "ํ์ฑํ๋? ๋ง?ํ?ฌ(Active)",
DlgDocMargins		: "ํ์?ด์ง ์ฌ๋ฐฑ",
DlgDocMaTop			: "์",
DlgDocMaLeft		: "์ผ์ชฝ",
DlgDocMaRight		: "์ค๋ฅธ์ชฝ",
DlgDocMaBottom		: "์๋",
DlgDocMeIndex		: "๋ฌธ์ ํค์๋ (์ฝค๋ง๋ก ๊ตฌ๋ถ)",
DlgDocMeDescr		: "๋ฌธ์ ์ค๋ช",
DlgDocMeAuthor		: "์์ฑ์?",
DlgDocMeCopy		: "์ ์๊ถ",
DlgDocPreview		: "๋ฏธ๋ฆฌ๋ณด๊ธฐ",

// Templates Dialog
Templates			: "ํํ๋ฆฟ",
DlgTemplatesTitle	: "๋ด์ฉ ํํ๋ฆฟ",
DlgTemplatesSelMsg	: "์?๋ํฐ์?์ ์ฌ์ฉํ  ํํ๋ฆฟ์? ์ ํ?ํ์ญ์์.<br>(์ง๊ธ๊น์ง ์์ฑ๋? ๋ด์ฉ์? ์ฌ๋?ผ์ง๋๋ค.):",
DlgTemplatesLoading	: "ํํ๋ฆฟ ๋ชฉ๋ก?์? ๋ถ๋ฌ์ค๋์ค์๋๋ค. ์ ์๋ง ๊ธฐ๋ค๋ ค์ฃผ์ญ์์.",
DlgTemplatesNoTpl	: "(ํํ๋ฆฟ์?ด ์์ต๋๋ค.)",

// About Dialog
DlgAboutAboutTab	: "About",
DlgAboutBrowserInfoTab	: "๋ธ๋?ผ์ฐ์  ์ ๋ณด",
DlgAboutVersion		: "๋ฒ์ ",
DlgAboutLicense		: "Licensed under the terms of the GNU Lesser General Public License",
DlgAboutInfo		: "For further information go to"
}