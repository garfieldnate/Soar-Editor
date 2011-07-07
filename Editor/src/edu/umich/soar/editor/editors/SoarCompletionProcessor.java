package edu.umich.soar.editor.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

import edu.umich.soar.editor.Activator;
import edu.umich.soar.editor.contexts.SoarContext;
import edu.umich.soar.editor.editors.datamap.Datamap;
import edu.umich.soar.editor.editors.datamap.DatamapAttribute;
import edu.umich.soar.editor.editors.datamap.DatamapNode;
import edu.umich.soar.editor.editors.datamap.DatamapNode.NodeType;

public class SoarCompletionProcessor extends TemplateCompletionProcessor
{

    private SoarConfiguration configuration = null;

    public SoarCompletionProcessor(SoarConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        ArrayList<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
        String allText = viewer.getDocument().get();
        String cursorText = allText.substring(0, offset); // text up to the
                                                          // cursor

        int lastWhitespace = 0;
        for (int i = offset - 1; i > 0 && lastWhitespace == 0; --i)
        {
            char c = allText.charAt(i);
            if (characterIsWhitespaceOrPunctuation(c))
            {
                lastWhitespace = i + 1;
            }
        }

        int lastRealWhitespace = 0;
        for (int i = offset - 1; i > 0 && lastRealWhitespace == 0; --i)
        {
            char c = allText.charAt(i);
            if (Character.isWhitespace(c))
            {
                lastRealWhitespace = i + 1;
            }
        }

        int secondLastRealWhitespace = 0;
        for (int i = lastRealWhitespace - 2; i > 0 && secondLastRealWhitespace == 0; --i)
        {
            char c = allText.charAt(i);
            if (Character.isWhitespace(c))
            {
                secondLastRealWhitespace = i + 1;
            }
        }

        String currentWord = cursorText.substring(lastWhitespace);
        String lastRealWord = cursorText.substring(lastRealWhitespace);
        String secondLastRealWord = (lastRealWhitespace - 1 >= secondLastRealWhitespace ? cursorText.substring(secondLastRealWhitespace, lastRealWhitespace - 1) : "");
        String secondLastWord = trimNonChars(secondLastRealWord);
        char lastChar = cursorText.length() > 0 ? cursorText.charAt(cursorText.length() - 1) : ' ';

        // determine paren depth
        int parenDepth = 0;
        int braceDepth = 0;
        int lastBrace = 0;
        char[] cursorChars = cursorText.toCharArray();
        for (int i = 0; i < cursorChars.length; ++i)
        {
            char c = cursorChars[i];
            if (c == '(')
            {
                ++parenDepth;
            }
            else if (c == ')')
            {
                --parenDepth;
            }
            if (c == '{')
            {
                ++braceDepth;
                if (braceDepth == 1)
                {
                    lastBrace = i;
                }
            }
            else if (c == '}')
            {
                --braceDepth;
            }
        }

        String thisRuleText = allText.substring(lastBrace);
        
        /*
         * if (cursorText.trim().length() == 0) { String ruleName =
         * row.getName().toLowerCase().replace(' ', '-'); String proposal =
         * "sp {" + ruleName + "\n   (state <s> ^\n\n}"; CompletionProposal
         * proposeSp = new CompletionProposal(proposal, 0, 0, 20 +
         * ruleName.length()); list.add(proposeSp); }
         */

        HashSet<String> proposals = new HashSet<String>();
        if (parenDepth > 0)
        {
            if (lastRealWord.startsWith("^") || lastRealWord.startsWith("-^"))
            {
                Collection<String> found = findDatamapAttributes(lastRealWord);
                if (found.isEmpty()) found = findDatamapAttributes();
                proposals.addAll(found);
            }
            else if (lastChar != '(')
            {
                Collection<String> found = findDatamapValues(secondLastRealWord);
                if (found.size() == 0) found = findDatamapValues();
                proposals.addAll(found);
            }
            if (lastChar != '^' && lastChar != '.')
            {
                proposals.addAll(findVariables(thisRuleText));
            }
            if (proposals.size() == 0)
            {
                proposals.addAll(findAttributes(thisRuleText));
                if (lastChar == '(')
                {
                    proposals.add("state <s>");
                }
            }
            if (proposals.size() == 0)
            {
                proposals.addAll(findVariables(allText));
            }
            if (proposals.size() == 0)
            {
                proposals.addAll(findAttributes(allText));
            }
        }
        else if (braceDepth == 0) 
        {
            proposals.add("sp {");
        }
        else
        {
            proposals.add("(");
        }
        ArrayList<String> proposalsList = new ArrayList<String>(proposals);
        Collections.sort(proposalsList, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2)
            {
                if (s1.startsWith("<") && !s2.startsWith("<"))
                {
                    return 1;
                }
                if (s2.startsWith("<") && !s1.startsWith("<"))
                {
                    return -1;
                }
                return s1.compareTo(s2);
            }
            
        });
        for (String proposal : proposalsList)
        {
            if (proposal.startsWith(currentWord) && !proposal.equals(currentWord))
            {
                String replacementString = proposal;
                int replacementOffset = lastWhitespace;
                int replacementLength = currentWord.length();
                int cursorPosition = proposal.length();
                CompletionProposal proposeSp = new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition);
                list.add(proposeSp);
            }
        }
        
        if (braceDepth == 0)
        {
            // TODO add template proposals
            /*
            String contextId = SoarContext.CONTEXT_ID;
            TemplateContextType context = Activator.getDefault().getContextTypeRegistry().getContextType(SoarContext.CONTEXT_ID);
            for (Template template : getTemplates(null))
            {
                
                template.getcon
                list.add(new TemplateProposal(template, , region, null))                
            }
            */
        }

        ICompletionProposal[] thisRet = list.toArray(new ICompletionProposal[] {});
        if (cursorText.length() == 0 || cursorText.charAt(cursorText.length() - 1) == '\n')
        {
            ICompletionProposal[] superRet = super.computeCompletionProposals(viewer, offset);
            ICompletionProposal[] ret = new ICompletionProposal[thisRet.length + superRet.length];
            System.arraycopy(thisRet, 0, ret, 0, thisRet.length);
            System.arraycopy(superRet, 0, ret, thisRet.length, superRet.length);
            return ret;
        }
        return thisRet;
    }
    
    private static String trimNonChars(String word)
    {
        if (word.isEmpty())
        {
            return word;
        }
        int start;
        for(start = 0; start < word.length(); ++start)
        {
            char c = word.charAt(start);
            if (Character.isLetter(c))
            {
                break;
            }
        }
        int end;
        for (end = word.length(); end >= start; --end)
        {
            char c = word.charAt(end - 1);
            if (Character.isLetter(c))
            {
                break;
            }
        }
        return word.substring(start, end);
    }

    public static class ProposalInfo implements Comparable<ProposalInfo>
    {
        public ProposalInfo(String replacementValueOnly)
        {
            replacementValue = replacementValueOnly;
            replacementDisplay = replacementValueOnly;
            informationWindow = "";
        }

        public ProposalInfo(String replacementValue, String replacementDisplay, String informationWindow)
        {
            this.replacementValue = replacementValue;
            this.replacementDisplay = replacementDisplay;
            this.informationWindow = informationWindow;
        }

        public String replacementValue;
        public String replacementDisplay;
        public String informationWindow;

        public int compareTo(ProposalInfo o)
        {
            if (replacementDisplay == null)
            {
                return replacementValue.compareTo(o.replacementValue);
            }

            return replacementDisplay.compareTo(o.replacementDisplay);
        }
    }

    private ArrayList<String> findVariables(String text)
    {
        HashSet<String> hash = new HashSet<String>();
        Pattern pattern = Pattern.compile("<([\\w-])+>");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find())
        {
            hash.add(matcher.group());
        }
        ArrayList<String> ret = new ArrayList<String>(hash);
        return ret;
    }

    private ArrayList<String> findAttributes(String text)
    {
        HashSet<String> hash = new HashSet<String>();
        Pattern pattern = Pattern.compile("\\^([\\w-]+)[\\s\\.]");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find())
        {
            String group = matcher.group(1);
            hash.add(group);
        }
        pattern = Pattern.compile("\\.([\\w-]+)[\\s\\.]");
        matcher = pattern.matcher(text);
        while (matcher.find())
        {
            String group = matcher.group(1);
            hash.add(group);
        }
        ArrayList<String> ret = new ArrayList<String>(hash);
        return ret;
    }

    private Collection<String> findDatamapAttributes()
    {
        return findDatamapAttributes(null);
    }

    private Collection<String> findDatamapAttributes(String soFar)
    {

        String last = null;

        if (soFar != null)
        {
            String[] parts = soFar.split("\\.");
            if (parts[0].startsWith("^")) parts[0] = parts[0].substring(1);
            if (parts.length > 0)
            {
                if (soFar.endsWith("."))
                {
                    last = parts[parts.length - 1];
                    if (last.length() == 0) last = null;
                }
                else if (parts.length > 1)
                {
                    last = parts[parts.length - 2];
                    if (last.length() == 0) last = null;
                }
            }
        }

        Collection<String> ret = new HashSet<String>();

        for (Datamap datamap : configuration.getEditor().getDatamaps())
        {
            for (ArrayList<DatamapAttribute> attributes : datamap.getAttributes().values())
            {
                for (DatamapAttribute attribute : attributes)
                {
                    if (last == null)
                    {
                        ret.add(attribute.name);
                    }
                    else
                    {
                        if (attribute.name.equals(last))
                        {
                            for (DatamapAttribute da : datamap.getAttributesFrom(attribute.to))
                                ret.add(da.name);
                        }
                    }
                }
            }
        }

        return ret;
    }

    private Collection<String> findDatamapValues()
    {
        return findDatamapValues(null);
    }

    private Collection<String> findDatamapValues(String secondLastWord)
    {
        String last = null;

        if (secondLastWord != null)
        {
            String[] parts = secondLastWord.split("\\.");
            if (parts[0].startsWith("^")) parts[0] = parts[0].substring(1);
            if (parts.length > 0)
            {
                last = parts[parts.length - 1];
                if (last.length() == 0) last = null;
            }
        }

        Collection<String> ret = new HashSet<String>();

        for (Datamap datamap : configuration.getEditor().getDatamaps())
        {
            for (DatamapNode node : datamap.getNodes().values())
            {
                if (node.type != NodeType.ENUMERATION)
                {
                    continue;
                }

                if (last == null)
                {
                    ret.addAll(node.values);
                }
                else
                {
                    List<DatamapAttribute> toAttrs = node.datamap.getAttributesTo(node.id);
                    boolean nodeOk = false;
                    for (int i = 0; i < toAttrs.size() && !nodeOk; ++i)
                    {
                        DatamapAttribute toAttr = toAttrs.get(i);
                        if (toAttr.name.equals(last))
                        {
                            ret.addAll(node.values);
                        }
                    }
                }
            }
        }

        return ret;
    }

    private boolean characterIsWhitespaceOrPunctuation(Character c)
    {
        final Character[] punctuation = { '(', ')', '.', '^' };
        HashSet<Character> hash = new HashSet<Character>();
        for (Character ch : punctuation)
        {
            hash.add(ch);
        }
        return (Character.isWhitespace(c) || hash.contains(c));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
     * getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return new char[] { '^', '<', '.', '[', '(' };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
     * getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage
     * ()
     */
    public String getErrorMessage()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
     * getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return new ContextInformationValidator(this);
    }

    @Override
    protected TemplateContextType getContextType(ITextViewer arg0, IRegion arg1)
    {
        // TODO Auto-generated method stub
        // return null;
        return Activator.getDefault().getContextTypeRegistry().getContextType(SoarContext.CONTEXT_ID);
    }

    @Override
    protected Image getImage(Template arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @Override protected Template[] getTemplates(String arg0) { // TODO
     * Auto-generated method stub return null; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType
     * (org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    /*
     * @Override protected TemplateContextType getContextType( ITextViewer
     * viewer, IRegion region ) { Table configurationTable =
     * configuration.getRow().getTable(); if (configurationTable == Table.RULES)
     * { TemplateContextType context =
     * SoarEditorUIPlugin.getDefault().getContextTypeRegistry
     * ().getContextType(SoarRuleContext.SOAR_CONTEXT_TYPE); if (context
     * instanceof SoarRuleContext) { ((SoarRuleContext)
     * context).setConfiguration(configuration); } return context; } else if
     * (configurationTable == Table.OPERATORS || configurationTable ==
     * Table.TAGS) { TemplateContextType context =
     * SoarEditorUIPlugin.getDefault(
     * ).getContextTypeRegistry().getContextType(SoarOperatorContext
     * .SOAR_CONTEXT_TYPE); if (context instanceof SoarOperatorContext) {
     * ((SoarOperatorContext) context).setConfiguration(configuration); } return
     * context; } return null; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage
     * (org.eclipse.jface.text.templates.Template)
     */
    /*
     * @Override protected Image getImage( Template template ) { ImageRegistry
     * registry = SoarEditorUIPlugin.getDefault() .getImageRegistry(); Image
     * image = registry.get( DEFAULT_IMAGE ); if( image == null ) {
     * ImageDescriptor desc = AbstractUIPlugin .imageDescriptorFromPlugin(
     * "com.soartech.soar.ide.ui", DEFAULT_IMAGE ); registry.put( DEFAULT_IMAGE,
     * desc ); image = registry.get( DEFAULT_IMAGE ); } return image; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates
     * (java.lang.String)
     */

    @Override
    protected Template[] getTemplates(String contextTypeId)
    {
        return Activator.getDefault().getTemplateStore().getTemplates();
    }
}