This branch is no longer being used for development! Use `new_master` instead.

---

When we first released OperaDriver as open source we made a decision to push
only the latest version of the code to Github. This has turned out to be a bad
decision, as it makes it very difficult for us to sync our internal repository
with Github.

We have therefore decided to push the full OperaDriver history. However Git
does not track branch renames, and so in order to avoid breaking anyone's
repository we are following the plan below:

 * Push our full OperaDriver history to a branch called `new_master`
 * Make `new_master` the default branch on Github
 * Replace all the files in `master` with this Readme
 * In one month, on 2011/05/13, `master` will be replaced with the tree and
   Git history in `new_master`.
 * This "new" `master` will be made the default branch


This will leave us working on `master` without introducing a mass of conflicts
for anyone who pulls to their local clone between now and then.

Stuart Knightley
2011/04/13
