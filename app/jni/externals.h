#ifndef _MACFUZZER_EXTERNALS
#define _MACFUZZER_EXTERNALS

/* Copied from bionic source repo because ndk is lacking
 * some important constants. */
/* commit 1f5706c7eb8aacb76fdfa3ef03944be229510b66 */
#include "capability.h"
#include "securebits.h"
#include "prctl.h"

/* The android-20 ndk doesn't support these */
#ifndef PR_CAPBSET_READ
#define PR_CAPBSET_READ 23
#endif
#ifndef PR_CAPBSET_DROP
#define PR_CAPBSET_DROP 24
#endif
#ifndef PR_SET_NO_NEW_PRIVS
#define PR_SET_NO_NEW_PRIVS 38
#endif
#ifndef CLONE_NEWIPC
# define CLONE_NEWIPC	0x08000000	/* New ipcs.  */
#endif
#ifndef CLONE_NEWNS
# define CLONE_NEWNS   0x00020000 /* Set to create new namespace.  */
#endif
#ifndef CLONE_NEWPID
# define CLONE_NEWPID	0x20000000	/* New pid namespace.  */
#endif
#ifndef CLONE_NEWUTS
# define CLONE_NEWUTS	0x04000000	/* New utsname group.  */
#endif
#ifndef CLONE_UNTRACED
# define CLONE_UNTRACED 0x00800000 /* Set if the tracing process can't
				      force CLONE_PTRACE on this clone.  */
#endif


#endif //_MACFUZZER_EXTERNALS
