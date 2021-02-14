package ch4;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


class ThreadLister {
    public static void main(String[] args) {
        Map<ThreadGroup, List<Thread>> map = Thread.getAllStackTraces().keySet().stream().collect(Collectors.groupingBy(Thread::getThreadGroup));
        map.forEach((group, lists)-> {
            System.out.println("ThreadGroup: " + group.getName());
            System.out.println("---------------------------");
            lists.forEach(t -> System.out.println("Name: " + t.getName() + " ID: "+t.getId() + " Status: "+t.getState()+" Daemon: "+t.isDaemon()));
        });
    }
}