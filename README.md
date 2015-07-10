￼READ ME
● Installation
○ OpenanewIntellijPluginmodule.
○ Addthepluginstotheplugins.xmlfile.
● Pushingtogitbranch
○ Pushthecommitstoagitbranch.
○ Youneedtoentertherepolinkandcommitidwhichusingthe
code review plugin.
● Crucibleregistration:
○ Registerwiththecrucibleusernameandpassword.
○ Thepluginloadstheprojectanduserslist.
○ Selecttheauthor,moderator,repositoryandreviewers.
○ Theplugincreatesareviewandsendthereviewidtothe
reviewers through slack.
○ ThereviewcanbealsocheckedinCrucible.
● Slack registration:
○ Registerwiththetokenavailableathttps://api.slack.com/web.
○ Thisisaonetimeregistration.
○ Thepluginloadsalltheusersamongwhichyouneedtochoose
the reviewers.
○ TheReviewrequestwillbesenttotheselectedreviewers
through Slack.
● Checkingoutthereview.
○ YoudiffwiththegitbranchwhichyoureceivedthroughSlack.
○ Makethereviewinthediffeditor.Branchisread­only.You
need to make the review there. Whenever you are making the review in a read­only mode you use the CrucibleReview plugin else you use the CrucibleRequest plugin.
● Makingreview
○ YoumakereviewintheDiffwindowusingtheshortcutofthe
plugin specified.
￼￼
￼○ Intellijatpresentdoesnthaveeditorpopupmenuindiff,but they will get it added in the new release.
● Loadingreview
○ LoadthereviewusingthereviewIdyoureceivedthroughSlack.
○ Nowyoucanmakethereviewusingtheshortcut.
○ Clickonalinewhichopensupacommentwindowandmake
the comment there.
○ Atoolgetaddedtothegutterwhichenablesyoutoaddreplies
and delete the comment.
○ Atoolgetaddedtotheveryfirstlinewhichenablesyoutoclear
and load the review again.
