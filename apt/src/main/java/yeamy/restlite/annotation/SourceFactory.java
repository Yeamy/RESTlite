package yeamy.restlite.annotation;

import java.lang.annotation.Annotation;

record SourceFactory<T extends Annotation>(T ann, String name) {
}
