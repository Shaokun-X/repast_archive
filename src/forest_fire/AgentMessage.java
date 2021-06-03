package forest_fire;


import repast.simphony.space.grid.GridPoint;

public class AgentMessage  {
	
	private int senderId;
	private GridPoint senderPosition;
	private int fireAssessment;
	
	public AgentMessage(int senderId, GridPoint pos, int fire) {
		this.setSenderId(senderId);
		setSenderPosition(pos);
		setFireAssessment(fire);
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public GridPoint getSenderPosition() {
		return senderPosition;
	}

	public void setSenderPosition(GridPoint senderPosition) {
		this.senderPosition = senderPosition;
	}

	public int getFireAssessment() {
		return fireAssessment;
	}

	public void setFireAssessment(int fireAssessment) {
		this.fireAssessment = fireAssessment;
	}
	


}