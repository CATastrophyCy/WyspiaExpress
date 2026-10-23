package org.cat.express.wyspiaexpress.config.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import java.io.*;
import java.util.*;

/** Generates accessor views, never modifies the model's source or compiler AST. */
@SupportedAnnotationTypes("org.cat.express.wyspiaexpress.config.ConfigModel")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class ConfigModelProcessor extends AbstractProcessor {
    private static final String ANNOTATION = "org.cat.express.wyspiaexpress.config.ConfigModel";
    private final Map<String, String> generated = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        TypeElement annotation = processingEnv.getElementUtils().getTypeElement(ANNOTATION);
        if (annotation == null) return false;
        for (Element element : round.getElementsAnnotatedWith(annotation)) {
            if (!(element instanceof TypeElement model)) continue;
            try { generate(model); }
            catch (RuntimeException | IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate config view: " + e.getMessage(), model);
            }
        }
        return true;
    }

    private void generate(TypeElement root) throws IOException {
        String viewName = root.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().toString().equals(ANNOTATION))
                .flatMap(a -> a.getElementValues().values().stream()).findFirst().orElseThrow().getValue().toString();
        if (!SourceVersion.isIdentifier(viewName) || SourceVersion.isKeyword(viewName)) throw new IllegalArgumentException("Invalid view name " + viewName);
        String pkg = processingEnv.getElementUtils().getPackageOf(root).getQualifiedName().toString();
        String qualified = pkg + "." + viewName;
        String previous = generated.putIfAbsent(qualified, root.getQualifiedName().toString());
        if (previous != null) {
            if (!previous.equals(root.getQualifiedName().toString())) throw new IllegalArgumentException("View " + qualified + " already generated for " + previous);
            return;
        }
        var nodes = new LinkedHashMap<String, TypeElement>();
        collect(root, nodes, new HashSet<>());
        var simpleNames = new HashSet<String>();
        for (TypeElement type : nodes.values()) {
            if (type == root) continue;
            if (!simpleNames.add(type.getSimpleName().toString()) || type.getSimpleName().contentEquals(viewName))
                throw new IllegalArgumentException("Nested model names must be unique inside " + viewName + ": " + type);
        }
        var text = new StringBuilder("package " + pkg + ";\n\n");
        text.append("/** Generated from ").append(root.getQualifiedName()).append("; edit the model declaration. */\n");
        emit(text, root, viewName, false, root);
        for (TypeElement type : nodes.values()) if (type != root) emit(text, type, type.getSimpleName().toString(), true, root);
        text.append("}\n");
        try (Writer out = processingEnv.getFiler().createSourceFile(qualified, root).openWriter()) { out.write(text.toString()); }
    }

    private List<VariableElement> fields(TypeElement type) {
        return processingEnv.getElementUtils().getAllMembers(type).stream()
                .filter(e -> e.getKind() == ElementKind.FIELD && e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.STATIC))
                .map(e -> (VariableElement)e).toList();
    }

    private boolean leaf(TypeMirror type) {
        if (type.getKind().isPrimitive()) return Set.of(TypeKind.BOOLEAN, TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE).contains(type.getKind());
        Element element = processingEnv.getTypeUtils().asElement(type);
        if (element == null) return false;
        return element.getKind() == ElementKind.ENUM || element.toString().equals("java.lang.String") || element.toString().equals("java.util.List");
    }

    private void collect(TypeElement type, Map<String, TypeElement> nodes, Set<String> visiting) {
        String name = type.getQualifiedName().toString();
        if (visiting.contains(name)) throw new IllegalArgumentException("Recursive model " + name);
        if (nodes.containsKey(name)) return;
        visiting.add(name); nodes.put(name, type);
        for (VariableElement field : fields(type)) if (!leaf(field.asType())) {
            Element nested = processingEnv.getTypeUtils().asElement(field.asType());
            if (!(nested instanceof TypeElement node) || fields(node).isEmpty()) throw new IllegalArgumentException("Unsupported config type: " + field.asType());
            collect(node, nodes, visiting);
        }
        visiting.remove(name);
    }

    private void emit(StringBuilder out, TypeElement type, String name, boolean nested, TypeElement root) {
        String indent = nested ? "    " : "";
        out.append(indent).append(nested ? "public static final class " : "public final class ").append(name)
                .append(" implements org.cat.express.wyspiaexpress.config.ConfigView<").append(type.getQualifiedName()).append("> {\n");
        out.append(indent).append("    private final ").append(type.getQualifiedName()).append(" model;\n");
        for (VariableElement field : fields(type)) if (!leaf(field.asType())) {
            out.append(indent).append("    public final ").append(viewType(field.asType(), root)).append(' ').append(field.getSimpleName()).append(";\n");
        }
        out.append(indent).append("    public ").append(name).append("() { this(new ").append(type.getQualifiedName()).append("()); }\n");
        out.append(indent).append("    public ").append(name).append('(').append(type.getQualifiedName()).append(" model) {\n")
                .append(indent).append("        this.model = java.util.Objects.requireNonNull(model);\n");
        for (VariableElement field : fields(type)) if (!leaf(field.asType())) {
            out.append(indent).append("        this.").append(field.getSimpleName()).append(" = new ").append(viewType(field.asType(), root))
                    .append("(model.").append(field.getSimpleName()).append(");\n");
        }
        out.append(indent).append("    }\n");
        out.append(indent).append("    @Override public ").append(type.getQualifiedName()).append(" model() { return model; }\n");
        for (VariableElement field : fields(type)) if (leaf(field.asType())) {
            out.append(indent).append("    public ").append(field.asType()).append(' ').append(field.getSimpleName())
                    .append("() { return org.cat.express.wyspiaexpress.config.ConfigValues.get(model, \"").append(field.getSimpleName())
                    .append("\", model.").append(field.getSimpleName()).append("); }\n");
        }
        if (nested) out.append(indent).append("}\n");
    }

    private String viewType(TypeMirror type, TypeElement root) {
        var element = processingEnv.getTypeUtils().asElement(type);
        return element.getSimpleName().toString();
    }
}
