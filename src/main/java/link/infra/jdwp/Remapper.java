package link.infra.jdwp;

public interface Remapper {
	String remapJNITypeSignature(String signature);
	String remapGenericClassSignature(String signature);
	String remapClassName(String className);
	// TODO: these will require traversing the inheritance graph, figure out how to do that cleanly
	// TODO: add descriptor
	String remapMethodName(String className, String methodName);
	// TODO: add descriptor
	String remapFieldName(String className, String fieldName);
	// TODO: pattern matching?
	// TODO: bytecode
	// TODO: line numbers
	// TODO: LVT?
}
