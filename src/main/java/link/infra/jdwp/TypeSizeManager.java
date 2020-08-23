package link.infra.jdwp;

public class TypeSizeManager {
	// Default to long (8 bytes)
	private int fieldID = 8;
	private int methodID = 8;
	private int objectID = 8;
	private int referenceTypeID = 8;
	private int frameID = 8;

	public synchronized void setSizes(int fieldID, int methodID, int objectID, int referenceTypeID, int frameID) {
		this.fieldID = fieldID;
		this.methodID = methodID;
		this.objectID = objectID;
		this.referenceTypeID = referenceTypeID;
		this.frameID = frameID;
	}

	public synchronized int getFieldID() {
		return fieldID;
	}

	public synchronized int getMethodID() {
		return methodID;
	}

	public synchronized int getObjectID() {
		return objectID;
	}

	public synchronized int getReferenceTypeID() {
		return referenceTypeID;
	}

	public synchronized int getFrameID() {
		return frameID;
	}
}
