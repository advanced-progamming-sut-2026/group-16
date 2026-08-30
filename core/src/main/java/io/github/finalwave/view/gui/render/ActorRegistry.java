package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public final class ActorRegistry<M, A extends Actor> {
    private final Map<M, A> actors = new IdentityHashMap<>();

    public void sync(Iterable<M> live, Function<M, A> spawn, BiConsumer<M, A> update, Consumer<A> despawn) {
        Set<M> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (M model : live) {
            seen.add(model);
            A actor = actors.get(model);
            if (actor == null) {
                actor = spawn.apply(model);
                actors.put(model, actor);
            }
            update.accept(model, actor);
        }
        Iterator<Map.Entry<M, A>> iterator = actors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<M, A> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                despawn.accept(entry.getValue());
                iterator.remove();
            }
        }
    }

    public A get(M model) {
        return actors.get(model);
    }

    public Collection<A> actors() {
        return Collections.unmodifiableCollection(actors.values());
    }

    public void clear(Consumer<A> despawn) {
        for (A actor : actors.values()) {
            despawn.accept(actor);
        }
        actors.clear();
    }

    public void setPlaying(boolean playing) {
        for (A actor : actors.values()) {
            if (actor instanceof PamActor pamActor) {
                pamActor.setPlaying(playing);
            }
        }
    }
}
