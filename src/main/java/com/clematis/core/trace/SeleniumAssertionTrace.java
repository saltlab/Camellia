package com.clematis.core.trace;

public class SeleniumAssertionTrace extends TraceObject/* implements EpisodeSource */{
	String outcome;
	int assertionID = -1;

	public SeleniumAssertionTrace() {
		super();
		setEpisodeSource(true);
	}

	public void setOutcome (String outcome) {
		if (outcome.contains("true")) {
			this.outcome = "Pass";
		} else if(outcome.contains("fail") || outcome.contains("incomplete")) {
			this.outcome = "Fail";
		} else {
			this.outcome = outcome;
		}
	}

	public String getOutcome () {
		return this.outcome;
	}

	public void setAssertionID(int parseInt) {
		this.assertionID = parseInt;	

	}

	public int getAssertionID() {
		return this.assertionID;		
	}

}
