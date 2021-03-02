#ifndef _VCE_ENUM_CONSTANT___H_
#define _VCE_ENUM_CONSTANT___H_ 1

enum conn_direction {
	INWARD,
	OUTWARD
};

enum conn_purpose {
	REAL_TIME,	  // real-time short connection
	TASK_DISTRIB, // task distribution
	RESULTS_AGG	  // results aggregate
};

#endif
