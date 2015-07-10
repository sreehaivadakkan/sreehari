**￼READ ME**

**Installation**

  Open a new Intellij Plugin module.
  
  Add the plugins to the plugins.xml file.
 
 **Pushing to git branch**
 
   Push the commits to a git branch.
   
  You need to enter the repo link and commit id while using the code review plugin.
 
 **Crucible registration:**
 
  Register with the crucible username and password.
  
  The plugin loads the project and users list.
  
  Select the author, moderator,repository and reviewers.
  
  The plugin creates a review and send the review id to the reviewers through slack.
  
  The review can be also checked in Crucible.

**Slack registration:**

 Register with the tokena vailable at https://api.slack.com/web.
 
 This is a onetime registration.
 
 The plugin loads all the users among which you need to choose the reviewers.
 
 The Review request will be sent to the selected reviewers through Slack.
 
 **Checking out the review.**
 
  You diff with the gitbranch which you received through Slack.
  
  Make the review in the diffeditor. Branch is read only. You need to make the review there. Whenever you are making   the review in a read­only mode you use the CrucibleReview plugin else you use the CrucibleRequest plugin.
 
 **Makingreview**
 
  You make review in the Diff window using the shortcut of the plugin specified.
  
  Intellij at present doesnt have editorpopupmenu in diff,but they will get it added in the new release.
 
 **Loading review**
 
  Load the review using the reviewId you received through Slack.
  
  Now you can make the review using the shortcut.
  
  Click on a line which opens up a commentwindow and make the comment there.
  
  A tool get added to the gutter which enables you to add replies and delete the comment.
  
  A tool get added to the very first line which enables you to clear and load the review again.
