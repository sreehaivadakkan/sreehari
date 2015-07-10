import com.intellij.diff.tools.util.DiffDataKeys;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.psi.*;
import com.intellij.util.IconUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;

/**
 * Created by sreehari.v on 14/05/15.
 */

//Class to store crucible comments details
class CommentDetails1
{
    boolean isReply;
    int crucibleLineNumber;
    RangeHighlighter reviewRangeHighlighter;
    RangeHighlighter reviewRangeHighlighter1;
    String crucibleCommentId;
    String reviewId;
    String reviewItemId;
    String parentId;
    String txt;
    CommentDetails1 parent;
    ArrayList<CommentDetails1> replyArray=new ArrayList<CommentDetails1>();
};

public class CrucibleReview extends AnAction {

    private String authenticationToken=null;
    private String revId;
    private String revItemId;
    private final String[] s = {null};
    private int reviewCommentCount=0;
    private boolean commentLoad=true;
    private ArrayList<CommentDetails1> commentDetailArray= new ArrayList<CommentDetails1>();
    private ArrayList <Integer>commentLines= new ArrayList();

    //Loads already present crucible comments.
    private  void commentLoader(final String REV_ID,String auth, final AnActionEvent E) {

        final Editor EDITOR=E.getData(PlatformDataKeys.EDITOR);
        final HttpClient HTTPCLIENT = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://localhost:8060/rest-service/reviews-v1/"+REV_ID+"/comments.json?FEAUTH="+auth);
        request.addHeader("content-type", "application/json");
        HttpResponse response = null;
        try {
            response = HTTPCLIENT.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        String jsonString;
        try {
            jsonString = EntityUtils.toString(response.getEntity());
            JSONObject jsonResult = new JSONObject(jsonString);
            final JSONArray DATA = jsonResult.getJSONArray("comments");

            for(int j=0;j<DATA.length();j++) {
                CommentDetails1 cd=new CommentDetails1();
                final JSONObject JSON_COMMENT = DATA.getJSONObject(j);
                final String TXT = JSON_COMMENT.getString("message");
                final String USER_DETAILS = JSON_COMMENT.getJSONObject("user").getString("userName");
                final String PERMANENT_ID = JSON_COMMENT.getString("permaId");
                final String REVIEW_ITEM_ID = JSON_COMMENT.getJSONObject("reviewItemId").getString("id");
                final int LINE_RANGE = JSON_COMMENT.getInt("toLineRange");

                cd.isReply=false;
                cd.crucibleLineNumber=LINE_RANGE;
                cd.crucibleCommentId=PERMANENT_ID;
                cd.reviewItemId=REVIEW_ITEM_ID;
                cd.reviewId=REV_ID;

                final StringBuilder STRING_BUILDER = new StringBuilder();
                STRING_BUILDER.append("/*");
                STRING_BUILDER.append("  Review added by : " + USER_DETAILS);
                STRING_BUILDER.append('\n');
                STRING_BUILDER.append(TXT);
                STRING_BUILDER.append('\n');
                STRING_BUILDER.append("*/");

                cd.txt=STRING_BUILDER.toString();

                int sample;
                for(sample=0;sample<reviewCommentCount;sample++) {
                    if(cd.crucibleLineNumber<commentDetailArray.get(sample).crucibleLineNumber) {
                        commentDetailArray.add(sample,cd);
                        slashModifier(TXT,sample);
                        break;
                    }
                }
                if(sample==reviewCommentCount) {
                    commentDetailArray.add(cd);
                    slashModifier(TXT,sample);
                }
                reviewCommentCount++;
                replyLoader(cd,E,JSON_COMMENT.getJSONArray("replies"));
            }


            final CountDownLatch C_LATCH = new CountDownLatch(1);
            new WriteCommandAction.Simple(E.getProject()) {
                @Override
                protected void run() throws Throwable {
                    for(int i=0;i<reviewCommentCount;i++) {
                        CommentDetails1 commentDet=commentDetailArray.get(i);
                        int linenumber = commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber);
                        if(commentDet.isReply==true) {
                            linenumber=commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber)-1-commentDet.parent.replyArray.indexOf(commentDet);
                        }
                        int lineStart = EDITOR.getDocument().getLineStartOffset(linenumber);
                        EDITOR.getDocument().insertString(lineStart,commentDet.txt + "\n");
                    }
                    C_LATCH.countDown();
                }
            }.execute();

            C_LATCH.await();

            for(int i=0;i<reviewCommentCount;i++) {

                final CommentDetails1 CD=commentDetailArray.get(i);
                final TextAttributes TEXTS = new TextAttributes();
                TEXTS.setBackgroundColor(Color.CYAN);
                TEXTS.setEffectType(EffectType.ROUNDED_BOX);
                if(CD.isReply==true) {
                    TEXTS.setBackgroundColor(Color.GREEN);
                    TEXTS.setEffectType(EffectType.ROUNDED_BOX);
                }
                final TextAttributes TEXTS1 = new TextAttributes();
                TEXTS1.setBackgroundColor(Color.green);
                TEXTS1.setEffectType(EffectType.ROUNDED_BOX);
                if(CD.isReply==true) {
                    TEXTS1.setBackgroundColor(Color.red);
                    TEXTS1.setEffectType(EffectType.ROUNDED_BOX);
                }
                final GutterIconRenderer GUTTER_ICON_RENDERER = new GutterIconRenderer() {
                    @NotNull
                    @Override
                    public Icon getIcon() {
                        return IconUtil.getAddIcon();
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    @Nullable
                    public String getTooltipText() {
                        return "Review Comment";
                    }

                    @Override
                    @Nullable
                    public ActionGroup getPopupMenuActions() {
                        ActionGroup actionGroup = new ActionGroup() {
                            @NotNull
                            @Override
                            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                AnAction Action[] = new AnAction[2];
                                Action[0] = new AnAction("Reply", "sample", IconUtil.getAddIcon()) {
                                    @Override
                                    public void actionPerformed(AnActionEvent anActionEvent) {
                                        replyToComment(CD, E);
                                    }
                                };
                                Action[1] = new AnAction("Delete", "sample", IconUtil.getRemoveIcon()) {
                                    @Override
                                    public void actionPerformed(AnActionEvent anActionEvent) {
                                        commentDelete(E, commentDetailArray.indexOf(CD), authenticationToken,true);
                                    }
                                };

                                return Action;
                            }
                        };
                        return actionGroup;
                    }
                };

                int linenumber = CD.crucibleLineNumber + commentOffset(CD.crucibleLineNumber);
                if(CD.isReply==true) {
                    linenumber=CD.crucibleLineNumber + commentOffset(CD.crucibleLineNumber)-1-CD.parent.replyArray.indexOf(CD);
                }
                final int LINE_START = EDITOR.getDocument().getLineStartOffset(linenumber);
                int commentLength = CD.txt.length();
                final int N_STOP = LINE_START + commentLength;
                CD.reviewRangeHighlighter = (EDITOR.getMarkupModel().addRangeHighlighter(LINE_START, N_STOP, 6000, TEXTS, HighlighterTargetArea.EXACT_RANGE));
                CD.reviewRangeHighlighter.setGutterIconRenderer(GUTTER_ICON_RENDERER);
                CD.reviewRangeHighlighter1 = (EDITOR.getMarkupModel().addRangeHighlighter(LINE_START + 4, LINE_START + 26, 6000, TEXTS1, HighlighterTargetArea.EXACT_RANGE));
                EDITOR.getFoldingModel().runBatchFoldingOperation(new Runnable() {
                    @Override
                    public void run() {
                        EDITOR.getFoldingModel().addFoldRegion(LINE_START, N_STOP, "Review Comment");
                    }
                });
            }


        } catch (InterruptedException e1){
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //Loads the reply to a specified crucible comment c.
    private void replyLoader(final CommentDetails1 C, final AnActionEvent E, final JSONArray JSON_REP) throws JSONException {
        for(int j=0;j<JSON_REP.length();j++) {
            JSONObject jsonReply=JSON_REP.getJSONObject(j);

            final CommentDetails1 COMMENT_DETAILS = new CommentDetails1();
            COMMENT_DETAILS.isReply = true;
            COMMENT_DETAILS.parent = C;
            COMMENT_DETAILS.parentId = C.crucibleCommentId;
            COMMENT_DETAILS.crucibleLineNumber = C.crucibleLineNumber+1+j;
            COMMENT_DETAILS.crucibleCommentId=jsonReply.getJSONObject("permId").getString("id");
            COMMENT_DETAILS.reviewId=C.reviewId;
            C.replyArray.add(COMMENT_DETAILS);

            final String TXT = jsonReply.getString("message");
            final String USER_DETAILS = jsonReply.getJSONObject("user").getString("userName");

            final StringBuilder STRING_BUILDER = new StringBuilder();
            STRING_BUILDER.append("/*");
            STRING_BUILDER.append("  Reply added by : " + USER_DETAILS);
            STRING_BUILDER.append('\n');
            STRING_BUILDER.append(TXT);
            STRING_BUILDER.append('\n');
            STRING_BUILDER.append("*/");

            COMMENT_DETAILS.txt=STRING_BUILDER.toString();
            int sample;
            for(sample=0;sample<reviewCommentCount;sample++) {
                if(COMMENT_DETAILS.crucibleLineNumber<=commentDetailArray.get(sample).crucibleLineNumber) {
                    if(COMMENT_DETAILS.crucibleLineNumber==commentDetailArray.get(sample).crucibleLineNumber) {
                        continue;
                    }
                    commentDetailArray.add(sample, COMMENT_DETAILS);
                    slashModifier(TXT,sample);
                    break;
                }
            }
            if(sample==reviewCommentCount) {
                commentDetailArray.add(COMMENT_DETAILS);
                slashModifier(TXT,sample);
            }
            reviewCommentCount++;
            replyLoader(COMMENT_DETAILS,E,jsonReply.getJSONArray("replies"));
        }
    }

    //Adds the crucible comment to its specific location.
    private int commentAdder(CommentDetails1 c,int off,boolean val) {
        if(val ==true) {
            int i = 0;
            for (; i < reviewCommentCount; i++) {

                System.out.println("rev cmnt cnt "+reviewCommentCount);
                CommentDetails1 commentDet=commentDetailArray.get(i);

                int linenumber = commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber);
                if(commentDet.isReply==true) {
                    linenumber=commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber)-1-commentDet.parent.replyArray.indexOf(commentDet);
                }
                System.out.println("line no  is "+linenumber);

                if (off < (linenumber)) {
                    commentDetailArray.add(i, c);
                    return i;
                }
            }
            commentDetailArray.add(c);
            return i;
        }
        else {
            int i=0;
            for (; i < reviewCommentCount; i++) {
                if (off < commentDetailArray.get(i).crucibleLineNumber) {
                    commentDetailArray.add(i, c);
                    return i;
                }
            }
            commentDetailArray.add(c);
            return i;
        }

    }

    //Finds the offset for a comment.
    private int commentOffset(int lineNo) {
        int sum=0;
        for(int i=0;i<reviewCommentCount;i++) {
            if(lineNo>commentDetailArray.get(i).crucibleLineNumber) {
                sum += commentLines.get(i) + 2;
            }
            else {
                return sum;
            }
        }
        return sum;
    }

    //Finds the no of lines to be added for crucible insertion.
    private int commentSpacer(int limit) {
        int num=0;
        for(int i=0;i<limit;i++) {
            num+=commentLines.get(i)+2;
        }
        return num;
    }

    //Makes a crucible comment.
    private String crucibleComment(String revId,String revItemId,String auth,String mes,String from,String to) throws IOException, JSONException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("http://localhost:8060/rest-service/reviews-v1/"+revId+"/reviewitems/"+revItemId+"/comments.json?FEAUTH="+auth);
        StringEntity params = null;
        try {
            params = new StringEntity("{\"message\" : \"" +
                    mes +
                    "\"," +
                    " \"draft\" : false," +
                    " \"deleted\" : false," +
                    " \"defectRaised\" : false," +
                    " \"defectApproved\" : false," +
                    "\"readStatus\" : \"READ\"," +
                    " \"fromLineRange\" : \"" +
                    from +
                    "\"," +
                    " \"toLineRange\" : \"" +
                    to +
                    "\"}");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String jsonString = EntityUtils.toString(response.getEntity());
        System.out.println(jsonString);
        JSONObject jsonResult = new JSONObject(jsonString);
        String id = jsonResult.getString("permaId");
        return id;
    }

    //Deletes the specified comment from crucible.
    private void crucibleDelete(String revId,String comId,String auth) throws IOException, JSONException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete("http://localhost:8060/rest-service/reviews-v1/"+revId+"/comments/"+comId+".json?FEAUTH="+auth);
        request.addHeader("content-type", "application/json");
        try {
            httpClient.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //Deletes the specified reply from crucible.
    private void crucibleReplyDelete(String revId,String comId,String parId,String auth) throws IOException, JSONException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete("http://localhost:8060/rest-service/reviews-v1/"+revId+"/comments/"+parId+"/replies/"+comId+".json?FEAUTH="+auth);
        request.addHeader("content-type", "application/json");
        try {
            httpClient.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //Makes a reply to the specified crucible comment.
    private String crucibleReply(String revId,String commentId,String auth,String mes,String from,String to) throws IOException, JSONException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("http://localhost:8060/rest-service/reviews-v1/"+revId+"/comments/"+commentId+"/replies.json?FEAUTH="+auth);
        StringEntity params = null;
        try {
            params = new StringEntity("{\"message\" : \"" +
                    mes +
                    "\"," +
                    " \"draft\" : false," +
                    " \"deleted\" : false," +
                    " \"defectRaised\" : false," +
                    " \"defectApproved\" : false," +
                    "\"readStatus\" : \"READ\"}");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String jsonString = EntityUtils.toString(response.getEntity());
        JSONObject jsonResult = new JSONObject(jsonString);
        JSONObject temp=jsonResult.getJSONObject("permaId");
        return temp.getString("id");
    }

    //Modifies the comment for crucible insertion.
    private String slashModifier(String input,int pos) {
        int nLines=1;
        StringBuilder retString= new StringBuilder();
        char tester;
        for(int i=0;i<input.length();i++) {
            tester=input.charAt(i);
            if(tester=='\n') {
                nLines++;
                retString.append("\\n");
                continue;
            }
            retString.append(tester);
        }
        commentLines.add(pos, nLines);
        return retString.toString();
    }

    //Deletes a comment from the IDE as well as crucible.
    private void commentDelete(final AnActionEvent E,  int i, final String AUTH, final boolean VAL) {
        final int TEMP=i;
        final Editor EDITOR=E.getData(PlatformDataKeys.EDITOR);
        final CountDownLatch COUNT_DOWN_LATCH= new CountDownLatch(1);
        new WriteCommandAction.Simple(E.getProject()) {
            @Override
            protected void run() throws Throwable {
                CommentDetails1 c= commentDetailArray.get(TEMP);
                for(CommentDetails1 replies:c.replyArray) {
                    commentDelete(E, commentDetailArray.indexOf(replies), AUTH,VAL);
                }
                if(VAL==true) {
                    if(c.isReply==false)
                        crucibleDelete(c.reviewId,c.crucibleCommentId,AUTH);
                    else {
                        c.parent.replyArray.remove(c);
                        crucibleReplyDelete(c.reviewId, c.crucibleCommentId, c.parentId, AUTH);
                    }
                }

                EDITOR.getMarkupModel().removeHighlighter(c.reviewRangeHighlighter);
                EDITOR.getMarkupModel().removeHighlighter(c.reviewRangeHighlighter1);
                final CommentDetails1 commentDet=commentDetailArray.get(TEMP);

                int linenumber = commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber);
                if(commentDet.isReply==true) {
                    linenumber=commentDet.crucibleLineNumber + commentOffset(commentDet.crucibleLineNumber)-2-commentDet.parent.replyArray.indexOf(commentDet);
                }
                final int lineStart = EDITOR.getDocument().getLineStartOffset(linenumber);

                new WriteCommandAction.Simple(E.getProject()) {
                    @Override
                    protected void run() throws Throwable {
                        EDITOR.getDocument().deleteString(lineStart, lineStart+commentDet.txt.length()+1);
                    }
                }.execute();
                commentDetailArray.remove(TEMP);
                commentLines.remove(TEMP);
                COUNT_DOWN_LATCH.countDown();
            }
        }.execute();

        try {
            COUNT_DOWN_LATCH.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        reviewCommentCount--;
    }

    //Crucible authentication.
    private void authenticate(final AnActionEvent e) {
        final File FILE = new File("cruccomment_authenticates.txt");
        if (!FILE.exists()) {
            final JFrame NEW_FRAME0 = new JFrame("Registration");
            final JPanel SAMPLE_PANEL = new JPanel();

            SAMPLE_PANEL.setPreferredSize(new Dimension(300, 200));
            SAMPLE_PANEL.setMaximumSize(new Dimension(300, 200));

            JLabel msg = new JLabel("You are not registered.Please register.");
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg1 = new JLabel("Enter Crucible Username");
            msg1.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg2 = new JLabel("Enter Crucible Password");
            msg2.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JTextField SAMPLE_TEXT_FIELD = new JTextField();
            SAMPLE_TEXT_FIELD.setMaximumSize(new Dimension(300, 30));
            SAMPLE_TEXT_FIELD.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JTextField SAMPLE_TEXT_FIELD1 = new JTextField();
            SAMPLE_TEXT_FIELD1.setMaximumSize(new Dimension(300, 30));
            SAMPLE_TEXT_FIELD1.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton jComment1 = new JButton();
            jComment1.setText("Register");
            jComment1.setAlignmentX(Component.LEFT_ALIGNMENT);

            ActionListener actionListener0 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String user = SAMPLE_TEXT_FIELD.getText();
                    String pass = SAMPLE_TEXT_FIELD1.getText();
                    HttpClient httpClient0 = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet("http://localhost:8060/rest-service/auth-v1/login.json?userName="+user+"&password="+pass);
                    HttpResponse response = null;
                    try {
                        response = httpClient0.execute(request);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String jsonString0 = null;
                    try {
                        jsonString0 = EntityUtils.toString(response.getEntity());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        JSONObject jsonResult0 = new JSONObject(jsonString0);
                        String authToken = jsonResult0.get("token").toString();
                        FILE.createNewFile();
                        FileWriter fw = null;
                        try {
                            fw = new FileWriter(FILE.getAbsoluteFile());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(authToken);
                        authenticationToken=authToken;
                        try {
                            bw.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(SAMPLE_PANEL, "You are registered");
                        NEW_FRAME0.setVisible(false);
                        reviewLoader(authenticationToken,e);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    httpClient0.getConnectionManager().shutdown();

                }
            };

            jComment1.addActionListener(actionListener0);

            SAMPLE_PANEL.setLayout(new BoxLayout(SAMPLE_PANEL, 3));
            SAMPLE_PANEL.add(msg);
            SAMPLE_PANEL.add(msg1);
            SAMPLE_PANEL.add(SAMPLE_TEXT_FIELD);
            SAMPLE_PANEL.add(msg2);
            SAMPLE_PANEL.add(SAMPLE_TEXT_FIELD1);
            SAMPLE_PANEL.add(jComment1);
            NEW_FRAME0.setContentPane(SAMPLE_PANEL);
            NEW_FRAME0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            NEW_FRAME0.pack();
            NEW_FRAME0.setVisible(true);
        }
        else {
            FileReader fw = null;
            try {
                fw = new FileReader(FILE.getAbsoluteFile());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            BufferedReader rw = new BufferedReader(fw);
            try {
                authenticationToken = rw.readLine();
                if(commentLoad==true) {
                    reviewLoader(authenticationToken,e);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                rw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    //Replies to a comment in the IDE which in turn gets reflected in Crucible.
    private void replyToComment(final CommentDetails1 C, final AnActionEvent E) {

        final CommentDetails1 COMMENT_DETAILS= new CommentDetails1();
        COMMENT_DETAILS.isReply=true;
        COMMENT_DETAILS.parent=C;
        final Editor EDITOR = E.getData(PlatformDataKeys.EDITOR);

        final JPanel JPANEL= new JPanel();
        JPANEL.setBackground(Color.gray);
        Dimension dimension=new Dimension();
        dimension.setSize(300, 200);
        JPANEL.setPreferredSize(dimension);
        JPANEL.setLayout(new BoxLayout(JPANEL, 3));
        JLabel jLabel= new JLabel();
        jLabel.setText("Add comment");
        final JEditorPane jEditorPane=new JEditorPane();
        JButton jComment= new JButton();
        jComment.setText("Comment");
        jComment.setForeground(Color.BLUE);
        jComment.setBounds(0, 50, 0, 50);

        JPANEL.add(jLabel);
        JPANEL.add(jEditorPane);
        JPANEL.add(jComment);
        final JFrame frame = new JFrame("Sample");

        ActionListener actionListener=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                s[0] = jEditorPane.getText();
                frame.setVisible(false);

                final int OFFSET = EDITOR.getDocument().getLineStartOffset(C.crucibleLineNumber+commentOffset(C.crucibleLineNumber))+C.txt.length();
                final String TXT=s[0];

                final TextAttributes TEXTS= new TextAttributes();
                TEXTS.setBackgroundColor(Color.green);
                TEXTS.setEffectType(EffectType.ROUNDED_BOX);

                final TextAttributes TEXTS1= new TextAttributes();
                TEXTS1.setBackgroundColor(Color.RED);
                TEXTS1.setEffectType(EffectType.ROUNDED_BOX);

                final GutterIconRenderer gutterIconRenderer = new GutterIconRenderer() {
                    @NotNull
                    @Override
                    public Icon getIcon() {
                        return IconUtil.getAddIcon();
                    }
                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }
                    @Override
                    public int hashCode() {
                        return 0;
                    }
                    @Override
                    @Nullable
                    public String getTooltipText() {
                        return "Review Comment";
                    }
                    @Override
                    @Nullable
                    public ActionGroup getPopupMenuActions() {
                        ActionGroup actionGroup=new ActionGroup() {
                            @NotNull
                            @Override
                            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                AnAction Action[] = new AnAction[2];
                                Action[0]=new AnAction("Reply","sample",IconUtil.getAddIcon()) {
                                    @Override
                                    public void actionPerformed(AnActionEvent anActionEvent) {

                                    }
                                };
                                Action[1]=new AnAction("Delete","sample",IconUtil.getRemoveIcon()) {
                                    @Override
                                    public void actionPerformed(AnActionEvent anActionEvent) {
                                        commentDelete(E,commentDetailArray.indexOf(COMMENT_DETAILS),authenticationToken,true);
                                    }
                                };

                                return Action;
                            }
                        };
                        return actionGroup;
                    }
                };

                final StringBuilder STRING_BUILDER=new StringBuilder();
                STRING_BUILDER.append("/*");
                STRING_BUILDER.append("  Reply added by : User 1");
                STRING_BUILDER.append('\n');
                STRING_BUILDER.append(TXT);
                STRING_BUILDER.append('\n');
                STRING_BUILDER.append("*/");

                COMMENT_DETAILS.txt=STRING_BUILDER.toString();

                new WriteCommandAction.Simple(E.getProject())
                {   @Override
                    protected void run() throws Throwable {
                        if(!COMMENT_DETAILS.txt.equals(null)) {
                            int linenumber=EDITOR.getDocument().getLineNumber(OFFSET)+1;
                            int lineStart= EDITOR.getDocument().getLineStartOffset(linenumber);
                            EDITOR.getDocument().insertString(lineStart, STRING_BUILDER.toString() + "\n");
                        }
                    }
                }.execute();


                int linenumber=EDITOR.getDocument().getLineNumber(OFFSET)+1;
                final int lineStart= EDITOR.getDocument().getLineStartOffset(linenumber);
                int commentLength = COMMENT_DETAILS.txt.length();
                final int   N_STOP = lineStart+commentLength;

                COMMENT_DETAILS.reviewRangeHighlighter=(EDITOR.getMarkupModel().addRangeHighlighter(lineStart, N_STOP, 6000, TEXTS, HighlighterTargetArea.EXACT_RANGE));
                COMMENT_DETAILS.reviewRangeHighlighter.setGutterIconRenderer(gutterIconRenderer);
                COMMENT_DETAILS.reviewRangeHighlighter1=(EDITOR.getMarkupModel().addRangeHighlighter(lineStart + 4, lineStart + 26, 6000, TEXTS1, HighlighterTargetArea.EXACT_RANGE));
                EDITOR.getFoldingModel().runBatchFoldingOperation(new Runnable() {
                    @Override
                    public void run() {
                        EDITOR.getFoldingModel().addFoldRegion(lineStart, N_STOP, "Review Comment");
                    }
                });

                COMMENT_DETAILS.parentId=C.crucibleCommentId;
                int position=commentAdder(COMMENT_DETAILS,linenumber,false);
                String crucibleText = slashModifier(TXT,position);
                try {
                    //To Be Modified
                    COMMENT_DETAILS.reviewId=revId;
                    COMMENT_DETAILS.reviewItemId=revItemId;
                    COMMENT_DETAILS.crucibleCommentId=crucibleReply(revId, C.crucibleCommentId, authenticationToken, crucibleText, String.valueOf(linenumber - commentSpacer(position)), String.valueOf(linenumber - commentSpacer(position)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                COMMENT_DETAILS.isReply=true;
                C.replyArray.add(COMMENT_DETAILS);
                COMMENT_DETAILS.crucibleLineNumber=C.crucibleLineNumber+1+C.replyArray.indexOf(COMMENT_DETAILS);
                reviewCommentCount++;

            }
        };

        jComment.addActionListener(actionListener);
        frame.setContentPane(JPANEL);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    //Allows the user to select the review and load it in IDE
    private void reviewLoader(final String auth, final AnActionEvent e) {

        final HttpClient HTTPCLIENT = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://localhost:8060/rest-service/reviews-v1.json?FEAUTH=" + auth);
        request.addHeader("content-type", "application/json");
        HttpResponse response = null;
        try {
            response = HTTPCLIENT.execute(request);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        String jsonString;
        try {
            jsonString = EntityUtils.toString(response.getEntity());
            JSONObject jsonResult = new JSONObject(jsonString);
            final JSONArray DATA = jsonResult.getJSONArray("reviewData");
            final String[] REVIEW_IDS = new String[DATA.length()];
            for (int i = 0; i < DATA.length(); i++) {
                REVIEW_IDS[i] = DATA.getJSONObject(i).getJSONObject("permaId").getString("id");
            }

            final JFrame NEW_FRAME0 = new JFrame("Select Review");
            final JPanel SAMPLE_PANEL = new JPanel();

            SAMPLE_PANEL.setPreferredSize(new Dimension(300,150));
            SAMPLE_PANEL.setMaximumSize(new Dimension(300, 150));

            JLabel msg = new JLabel("Please select the review ID.");
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg1 = new JLabel("Please select the review Item ID");
            msg1.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JComboBox comboBox=new JComboBox(REVIEW_IDS);
            final JComboBox comboBox1=new JComboBox();


            JButton jComment1= new JButton();
            jComment1.setText("Load");
            jComment1.setAlignmentX(Component.LEFT_ALIGNMENT);

            ActionListener actionListener0=new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    NEW_FRAME0.setVisible(false);
                    final TextAttributes CLEAR_TEXT = new TextAttributes();
                    final GutterIconRenderer GUTTER_LOADER = new GutterIconRenderer() {
                        @NotNull
                        @Override
                        public Icon getIcon() {
                            return IconUtil.getRemoveIcon();
                        }

                        @Override
                        public boolean equals(Object o) {
                            return false;
                        }

                        @Override
                        public int hashCode() {
                            return 0;
                        }

                        @Override
                        @Nullable
                        public String getTooltipText() {
                            return "Clear";
                        }

                        @Override
                        @Nullable
                        public ActionGroup getPopupMenuActions() {
                            ActionGroup actionGroup = new ActionGroup() {
                                @NotNull
                                @Override
                                public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                    AnAction Action[] = new AnAction[2];
                                    Action[0] = new AnAction("Clear", "sample", IconUtil.getRemoveIcon()) {
                                        @Override
                                        public void actionPerformed(AnActionEvent anActionEvent) {
                                            for(int i=0;commentDetailArray.size()!=0;) {
                                                commentDelete(e,i,authenticationToken,false);}
                                        }
                                    };
                                    Action[0] = new AnAction("Load", "sample", IconUtil.getRemoveIcon()) {
                                        @Override
                                        public void actionPerformed(AnActionEvent anActionEvent) {
                                            authenticate(e);
                                        }
                                    };

                                    return Action;
                                }
                            };
                            return actionGroup;
                        }
                    };
                    e.getData(PlatformDataKeys.EDITOR).getMarkupModel().addRangeHighlighter(0,0, 6000, CLEAR_TEXT, HighlighterTargetArea.EXACT_RANGE).setGutterIconRenderer(GUTTER_LOADER);
                    commentLoader(revId, authenticationToken, e);
                    commentLoad = false;
                }
            };

            ActionListener actionListener1=new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    revId = REVIEW_IDS[comboBox.getSelectedIndex()];
                    final HttpClient HTTPCLIENT = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet("http://localhost:8060/rest-service/reviews-v1/" + revId + "/reviewitems.json?FEAUTH=" + auth);
                    request.addHeader("content-type", "application/json");
                    HttpResponse response = null;
                    try {
                        response = HTTPCLIENT.execute(request);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String jsonString;
                    try {
                        jsonString = EntityUtils.toString(response.getEntity());
                        JSONObject jsonResult = new JSONObject(jsonString);
                        final JSONArray DATA = jsonResult.getJSONArray("reviewItem");
                        final String[] REVIEW_IDS = new String[DATA.length()];
                        comboBox1.removeAllItems();
                        for (int i = 0; i < DATA.length(); i++) {
                            REVIEW_IDS[i] = DATA.getJSONObject(i).getJSONObject("permId").getString("id");
                            comboBox1.addItem(REVIEW_IDS[i]);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            };

            ActionListener actionListener3=new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if(comboBox1.getItemCount()!=0) {
                        revItemId = comboBox1.getSelectedItem().toString();
                    }
                }
            };

            comboBox1.addActionListener(actionListener3);
            comboBox.addActionListener(actionListener1);
            jComment1.addActionListener(actionListener0);

            SAMPLE_PANEL.setLayout(new BoxLayout(SAMPLE_PANEL, 3));
            SAMPLE_PANEL.add(msg);
            SAMPLE_PANEL.add(comboBox);
            SAMPLE_PANEL.add(msg1);
            SAMPLE_PANEL.add(comboBox1);
            SAMPLE_PANEL.add(jComment1);
            NEW_FRAME0.setContentPane(SAMPLE_PANEL);
            NEW_FRAME0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            NEW_FRAME0.pack();
            NEW_FRAME0.setVisible(true);

        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void actionPerformed(final AnActionEvent e) {

        e.getData(PlatformDataKeys.EDITOR).getDocument().setReadOnly(false);

        if (commentLoad == true) {
            authenticate(e);
            //commentLoad=false;
        }
        else {
            final CommentDetails1 COMMENT_DETAILS = new CommentDetails1();
            COMMENT_DETAILS.isReply = false;
            final int[] k = {0, 0};
            final Editor EDITOR = e.getData(PlatformDataKeys.EDITOR);

            final EditorMouseListener m = new EditorMouseListener() {
                @Override
                public void mousePressed(EditorMouseEvent editorMouseEvent) {
                }

                @Override
                public void mouseClicked(EditorMouseEvent ev) {
                    JPanel jPanel = new JPanel();
                    jPanel.setBackground(Color.gray);
                    Dimension dimension = new Dimension();
                    dimension.setSize(300, 200);
                    jPanel.setPreferredSize(dimension);
                    jPanel.setLayout(new BoxLayout(jPanel, 3));
                    JLabel jLabel = new JLabel();
                    jLabel.setText("Add comment");

                    final JEditorPane jEditorPane = new JEditorPane();
                    JButton jComment = new JButton();
                    jComment.setText("Comment");
                    jComment.setForeground(Color.BLUE);
                    jComment.setBounds(0, 50, 0, 50);
                    jPanel.add(jLabel);
                    jPanel.add(jEditorPane);
                    jPanel.add(jComment);
                    final JFrame frame = new JFrame("Sample");
                    ActionListener actionListener = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            s[0] = jEditorPane.getText();
                            frame.setVisible(false);
                            final LogicalPosition LP = EDITOR.getCaretModel().getLogicalPosition();
                            final int OFFSET = EDITOR.logicalPositionToOffset(LP);
                            final String TXT = s[0];

                            final TextAttributes TEXTS = new TextAttributes();
                            TEXTS.setBackgroundColor(Color.CYAN);
                            TEXTS.setEffectType(EffectType.ROUNDED_BOX);

                            final TextAttributes TEXTS1 = new TextAttributes();
                            TEXTS1.setBackgroundColor(Color.green);
                            TEXTS1.setEffectType(EffectType.ROUNDED_BOX);

                            final GutterIconRenderer gutterIconRenderer = new GutterIconRenderer() {
                                @NotNull
                                @Override
                                public Icon getIcon() {
                                    return IconUtil.getAddIcon();
                                }

                                @Override
                                public boolean equals(Object o) {
                                    return false;
                                }

                                @Override
                                public int hashCode() {
                                    return 0;
                                }

                                @Override
                                @Nullable
                                public String getTooltipText() {
                                    return "Review Comment";
                                }

                                @Override
                                @Nullable
                                public ActionGroup getPopupMenuActions() {
                                    ActionGroup actionGroup = new ActionGroup() {
                                        @NotNull
                                        @Override
                                        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                            AnAction Action[] = new AnAction[2];
                                            Action[0] = new AnAction("Reply", "sample", IconUtil.getAddIcon()) {
                                                @Override
                                                public void actionPerformed(AnActionEvent anActionEvent) {
                                                    replyToComment(COMMENT_DETAILS, e);
                                                }
                                            };
                                            Action[1] = new AnAction("Delete", "sample", IconUtil.getRemoveIcon()) {
                                                @Override
                                                public void actionPerformed(AnActionEvent anActionEvent) {
                                                    commentDelete(e, commentDetailArray.indexOf(COMMENT_DETAILS), authenticationToken,true);
                                                }
                                            };
                                            return Action;
                                        }
                                    };
                                    return actionGroup;
                                }
                            };

                            final StringBuilder STRING_BUILDER = new StringBuilder();
                            STRING_BUILDER.append("/*");
                            STRING_BUILDER.append("  Review added by : User 1");
                            STRING_BUILDER.append('\n');
                            STRING_BUILDER.append(TXT);
                            STRING_BUILDER.append('\n');
                            STRING_BUILDER.append("*/");
                            COMMENT_DETAILS.txt=STRING_BUILDER.toString();

                            final CountDownLatch countDownLatch1=new CountDownLatch(1);
                            new WriteCommandAction.Simple(e.getProject()) {
                                @Override
                                protected void run() throws Throwable {
                                    if (!COMMENT_DETAILS.txt.equals(null)) {
                                        int linenumber = EDITOR.getDocument().getLineNumber(OFFSET);
                                        int lineStart = EDITOR.getDocument().getLineStartOffset(linenumber);
                                        EDITOR.getDocument().insertString(lineStart, STRING_BUILDER.toString() + "\n");
                                        countDownLatch1.countDown();
                                    }
                                }
                            }.execute();
                            try {
                                countDownLatch1.await();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }

                            int linenumber = EDITOR.getDocument().getLineNumber(OFFSET);
                            final int lineStart = EDITOR.getDocument().getLineStartOffset(linenumber);
                            int commentLength = COMMENT_DETAILS.txt.length();
                            final int N_STOP = lineStart + commentLength;

                            COMMENT_DETAILS.reviewRangeHighlighter = (EDITOR.getMarkupModel().addRangeHighlighter(lineStart, N_STOP, 6000, TEXTS, HighlighterTargetArea.EXACT_RANGE));
                            COMMENT_DETAILS.reviewRangeHighlighter.setGutterIconRenderer(gutterIconRenderer);
                            COMMENT_DETAILS.reviewRangeHighlighter1 = (EDITOR.getMarkupModel().addRangeHighlighter(lineStart + 4, lineStart + 28, 6000, TEXTS1, HighlighterTargetArea.EXACT_RANGE));
                            EDITOR.getFoldingModel().runBatchFoldingOperation(new Runnable() {
                                @Override
                                public void run() {
                                    EDITOR.getFoldingModel().addFoldRegion(lineStart, N_STOP, "Review Comment");
                                }
                            });
                            k[0] = 1;

                            int position = commentAdder(COMMENT_DETAILS,linenumber,true);

                            String crucibleText = slashModifier(TXT, position);
                            try {
                                COMMENT_DETAILS.reviewId = revId;
                                COMMENT_DETAILS.reviewItemId = revItemId;
                                COMMENT_DETAILS.crucibleCommentId = crucibleComment(revId, revItemId, authenticationToken, crucibleText, String.valueOf(LP.line - commentSpacer(position)), String.valueOf(LP.line - commentSpacer(position)));
                            }
                            catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            COMMENT_DETAILS.crucibleLineNumber = LP.line - commentSpacer(position);
                            reviewCommentCount++;

                        }
                    };
                    jComment.addActionListener(actionListener);
                    frame.setContentPane(jPanel);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }

                @Override
                public void mouseReleased(EditorMouseEvent editorMouseEvent) {

                }

                @Override
                public void mouseEntered(EditorMouseEvent editorMouseEvent) {
                }

                @Override
                public void mouseExited(EditorMouseEvent editorMouseEvent) {
                }
            };
            class Th extends Thread {
                public void run() {
                    while (true) {
                        if (k[0] == 1) {
                            EDITOR.removeEditorMouseListener(m);
                            break;
                        }
                        else {
                            k[1] = 0;
                        }
                    }
                }
            }
            EDITOR.addEditorMouseListener(m);
            Th newThread = new Th();
            newThread.start();
        }
    }
}
