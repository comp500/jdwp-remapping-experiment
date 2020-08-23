package link.infra.jdwp;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.Mapped;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TinyTreeRemapper implements Remapper {
	// intermediary
	private final String sourceNamespace = "official";
	private final String targetNamespace = "named";

	private final Map<String, ClassDef> classDefMap = new HashMap<>();

	public TinyTreeRemapper(TinyTree tree) {
		for (ClassDef def : tree.getClasses()) {
			String name = def.getName(sourceNamespace);
			if (name != null) {
				classDefMap.put(name, def);
			}
		}
	}

	private <T extends Mapped> String remapNameSearch(Collection<T> collection, String name) {
		for (T def : collection) {
			if (def.getName(sourceNamespace).equals(name)) {
				String mappedName = def.getName(targetNamespace);
				if (mappedName != null && mappedName.length() > 0) {
					return mappedName;
				}
				return name;
			}
		}
		return name;
	}

	@Override
	public String remapJNITypeSignature(String signature) {
		System.out.println("Should remap " + signature);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < signature.length(); i++) {
			if (signature.charAt(i) == 'L') {
				int end = signature.indexOf(';', i);
				sb.append('L');
				sb.append(remapClassName(signature.substring(i + 1, end)));
				sb.append(';');
				i += end - i;
			} else {
				sb.append(signature.charAt(i));
			}
		}
		//return signature;
		System.out.println("Remaps to " + sb.toString());
		return sb.toString();
	}

	@Override
	public String remapGenericClassSignature(String signature) {
		System.out.println("Generic sig " + signature);
		return signature;
	}

	@Override
	public String remapClassName(String className) {
		// TODO: sanitization of class names - inner classes/lambdas?
		ClassDef def = classDefMap.get(className);
		if (def != null) {
			String mappedName = def.getName(targetNamespace);
			if (mappedName != null && mappedName.length() > 0) {
				return mappedName;
			}
		}
		return className;
	}

	@Override
	public String remapMethodName(String className, String methodName) {
		// TODO: does this work for inherited methods?
		ClassDef def = classDefMap.get(className);
		return def != null ? remapNameSearch(def.getMethods(), methodName) : methodName;
	}

	@Override
	public String remapFieldName(String className, String fieldName) {
		ClassDef def = classDefMap.get(className);
		return def != null ? remapNameSearch(def.getFields(), fieldName) : fieldName;
	}
}
