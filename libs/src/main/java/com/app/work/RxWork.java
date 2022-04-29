package com.app.work;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.InputMerger;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.impl.utils.DurationApi26Impl;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RxWork {

    private final Class<? extends ListenableWorker> workerClass;

    private long backoffDelayDuration;
    private long minimumRetentionDuration;
    private long initialDelay;

    private BackoffPolicy backoffPolicy;

    private Constraints.Builder constraints = new Constraints.Builder();
    private Data inputData;
    private OutOfQuotaPolicy expedited;
    private Set<String> mTags;
    private long repeatIntervalTime = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS;
    private long flexIntervalTime = PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS;

    private Class<? extends InputMerger> inputMerger;

    public RxWork(@NonNull Class<? extends ListenableWorker> workerClass) {
        this.workerClass = workerClass;
    }

    public static RxWork form(Class<? extends ListenableWorker> workerClass) {
        return new RxWork(workerClass);
    }


    /**
     * 是否在设备处于充电时运行
     */
    public RxWork setRequiresCharging(boolean requiresCharging) {
        constraints.setRequiresCharging(requiresCharging);
        return this;
    }

    /**
     * 运行时设备是否应该是空闲的
     */
    @RequiresApi(23)
    public RxWork setRequiresDeviceIdle(boolean requiresDeviceIdle) {
        constraints.setRequiresDeviceIdle(requiresDeviceIdle);
        return this;
    }

    /**
     * 设置在设备的什么网络状态下运行任务
     */
    public RxWork setRequiredNetworkType(@NonNull NetworkType networkType) {
        constraints.setRequiredNetworkType(networkType);
        return this;
    }

    /**
     * 设置是否运行在电池电量充足的状态下
     */
    public RxWork setRequiresBatteryNotLow(boolean requiresBatteryNotLow) {
        constraints.setRequiresBatteryNotLow(requiresBatteryNotLow);
        return this;
    }

    /**
     * 设置是否运行在存储足够的状态下
     */
    public RxWork setRequiresStorageNotLow(boolean requiresStorageNotLow) {
        constraints.setRequiresStorageNotLow(requiresStorageNotLow);
        return this;
    }

    @RequiresApi(24)
    public RxWork addContentUriTrigger(
            @NonNull Uri uri,
            boolean triggerForDescendants) {
        constraints.addContentUriTrigger(uri, triggerForDescendants);
        return this;
    }

    /**
     * 设置从第一次检测到内容更改到计划的时间所允许的延迟
     *
     * @param duration
     * @param timeUnit
     * @return
     */
    @RequiresApi(24)
    @NonNull
    public RxWork setTriggerContentUpdateDelay(
            long duration,
            @NonNull TimeUnit timeUnit) {
        constraints.setTriggerContentUpdateDelay(duration, timeUnit);
        return this;
    }

    /**
     * 设置从第一次检测到内容更改到计划的时间所允许的延迟
     *
     * @param duration
     * @return
     */
    @RequiresApi(26)
    @NonNull
    public RxWork setTriggerContentUpdateDelay(Duration duration) {
        constraints.setTriggerContentUpdateDelay(duration);
        return this;
    }

    /**
     * 设置从第一次检测到内容更改到计划的时间所允许的最大延迟
     */
    @RequiresApi(24)
    @NonNull
    public RxWork setTriggerContentMaxDelay(
            long duration,
            @NonNull TimeUnit timeUnit) {
        constraints.setTriggerContentMaxDelay(duration, timeUnit);
        return this;
    }

    /**
     * 设置从第一次检测到内容更改到计划的时间所允许的最大延迟
     */
    @RequiresApi(26)
    @NonNull
    public RxWork setTriggerContentMaxDelay(Duration duration) {
        constraints.setTriggerContentMaxDelay(duration);
        return this;
    }


    /**
     * 设置工作的后退策略和后退延迟。
     */
    public RxWork setBackoffCriteria(
            @NonNull BackoffPolicy backoffPolicy,
            long backoffDelay,
            @NonNull TimeUnit timeUnit) {
        this.backoffPolicy = backoffPolicy;
        this.backoffDelayDuration = timeUnit.toMillis(backoffDelay);
        return this;
    }

    /**
     * 设置工作的后退策略和后退延迟。
     *
     * @param backoffPolicy
     * @param backoffDelayDuration
     * @return
     */
    public RxWork setBackoffCriteria(
            @NonNull BackoffPolicy backoffPolicy,
            long backoffDelayDuration) {
        this.backoffPolicy = backoffPolicy;
        this.backoffDelayDuration = backoffDelayDuration;
        return this;
    }

    /**
     * 设置工作的后退策略和后退延迟
     */
    @RequiresApi(26)
    public RxWork setBackoffCriteria(
            @NonNull BackoffPolicy backoffPolicy,
            @NonNull Duration duration) {
        this.backoffPolicy = backoffPolicy;
        this.backoffDelayDuration = DurationApi26Impl.toMillisCompat(duration);
        return this;
    }


    /**
     * 将输入数据添加到工作中。如果一个worker在它的链中有先决条件，这个数据将使用inputmerge与先决条件的输出合并。
     */
    public RxWork setInputData(@NonNull Data inputData) {
        this.inputData = inputData;
        return this;
    }

    /**
     * 为工作添加标记。您可以通过标记查询和取消工作。标记对于模块或库查找和操作它们自己的工作特别有用。
     */
    public RxWork addTag(@NonNull String tag) {
        if (mTags == null) {
            mTags = new HashSet<>();
        }
        mTags.add(tag);
        return this;
    }

    /**
     * 指定此工作的结果应至少保留指定的时间。在此时间过后，当没有未决的相关作业时，WorkManager可以自行裁剪结果。
     */
    public RxWork keepResultsForAtLeast(long duration, @NonNull TimeUnit timeUnit) {
        this.minimumRetentionDuration = timeUnit.toMillis(duration);
        return this;
    }

    /**
     * 指定此工作的结果应至少保留指定的时间。在此时间过后，当没有未决的相关作业时，WorkManager可以自行裁剪结果。
     */
    @RequiresApi(26)
    public RxWork keepResultsForAtLeast(@NonNull Duration duration) {
        this.minimumRetentionDuration = DurationApi26Impl.toMillisCompat(duration);
        return this;
    }

    /**
     * 设置WorkRequest的初始延迟。
     */
    public RxWork setInitialDelay(long duration, @NonNull TimeUnit timeUnit) {
        this.initialDelay = timeUnit.toMillis(duration);
        return this;
    }

    /**
     * 设置WorkRequest的初始延迟。
     */
    @RequiresApi(26)
    public RxWork setInitialDelay(@NonNull Duration duration) {
        this.initialDelay = DurationApi26Impl.toMillisCompat(duration);
        return this;
    }

    /**
     * 设置WorkRequest的重复间隔
     */
    public RxWork setRepeatInterval(long duration, @NonNull TimeUnit timeUnit) {
        this.repeatIntervalTime = timeUnit.toMillis(duration);
        return this;
    }

    /**
     * 设置WorkRequest的重复间隔
     */
    @RequiresApi(26)
    public RxWork setRepeatInterval(@NonNull Duration duration) {
        this.repeatIntervalTime = DurationApi26Impl.toMillisCompat(duration);
        return this;
    }

    /**
     * 设置WorkRequest的持续时间单位
     */
    public RxWork setFlexInterval(long duration, @NonNull TimeUnit timeUnit) {
        this.flexIntervalTime = timeUnit.toMillis(duration);
        return this;
    }

    /**
     * 设置WorkRequest的持续时间单位
     */
    @RequiresApi(26)
    public RxWork setFlexInterval(@NonNull Duration duration) {
        this.flexIntervalTime = DurationApi26Impl.toMillisCompat(duration);
        return this;
    }

    /**
     * 将WorkRequest标记为对用户重要的。在这种情况下，WorkManager向操作系统提供了一个额外的信号，表明这项工作很重要。
     */
    public RxWork setExpedited(@NonNull OutOfQuotaPolicy policy) {
        this.expedited = policy;
        return this;
    }

    /**
     * 为OneTimeWorkRequest指定InputMerger类名。
     * 在工作线程运行之前，它们从它们的父工作线程接收输入数据，以及通过setInputData(Data)
     * 直接指定给它们的任何东西。InputMerger接受所有这些对象，并将它们转换为单个合并数据，以用作工作者输入。
     *
     * @param inputMerger
     * @return
     */
    public RxWork setInputMerger(@NonNull Class<? extends InputMerger> inputMerger) {
        this.inputMerger = inputMerger;
        return this;
    }

    /**
     * 一次性工作任务请求
     *
     * @return
     */
    public OneTimeWorkRequest oneTimeWorkRequest() {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(workerClass)
                .keepResultsForAtLeast(minimumRetentionDuration, TimeUnit.MILLISECONDS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints.build());
        if (backoffPolicy != null) {
            builder.setBackoffCriteria(backoffPolicy, backoffDelayDuration, TimeUnit.MILLISECONDS);
        }
        if (inputData != null) {
            builder.setInputData(inputData);
        }
        if (expedited != null) {
            builder.setExpedited(expedited);
        }
        if (mTags != null) {
            for (String mTag : mTags) {
                builder.addTag(mTag);
            }
        }
        if (inputMerger != null) {
            builder.setInputMerger(inputMerger);
        }
        return builder.build();
    }

    /**
     * 周期性工作任务请求
     *
     * @return
     */
    public PeriodicWorkRequest periodicWorkRequest() {
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(workerClass,
                repeatIntervalTime, TimeUnit.MILLISECONDS,
                flexIntervalTime, TimeUnit.MILLISECONDS)
                .keepResultsForAtLeast(minimumRetentionDuration, TimeUnit.MILLISECONDS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints.build());
        if (backoffPolicy != null) {
            builder.setBackoffCriteria(backoffPolicy, backoffDelayDuration, TimeUnit.MILLISECONDS);
        }
        if (inputData != null) {
            builder.setInputData(inputData);
        }
        if (expedited != null) {
            builder.setExpedited(expedited);
        }
        if (mTags != null) {
            for (String mTag : mTags) {
                builder.addTag(mTag);
            }
        }
        return builder.build();
    }

    /**
     * 一次性任务
     *
     * @return
     */
    public OneTimeWork oneTimeWork() {
        return new OneTimeWork(oneTimeWorkRequest());
    }

    /**
     * 周期性任务
     *
     * @return
     */
    public PeriodicWork periodicWork() {
        return new PeriodicWork(periodicWorkRequest());
    }

    public static final class OneTimeWork {
        private OneTimeWorkRequest workRequest;

        private OneTimeWork(OneTimeWorkRequest workRequest) {
            this.workRequest = workRequest;
        }

        /**
         * 获取workRequest
         *
         * @return
         */
        public OneTimeWorkRequest getWork() {
            return workRequest;
        }

        /**
         * 直接执行任务
         *
         * @param context
         * @return
         */
        public Operation enqueue(Context context) {
            return WorkManager.getInstance(context).enqueue(workRequest);
        }

        /**
         * 开启执行一次性任务
         *
         * @param context
         * @return
         */
        public WorkContinuation beginWith(Context context) {
            return WorkManager.getInstance(context).beginWith(workRequest);
        }

        /**
         * 开启唯一性工作任务
         *
         * @param context
         * @param uniqueWorkName
         * @param existingWorkPolicy
         * @return
         */
        public WorkContinuation beginUniqueWork(@NonNull Context context, @NonNull String uniqueWorkName,
                                                @NonNull ExistingWorkPolicy existingWorkPolicy) {
            return WorkManager.getInstance(context).beginUniqueWork(uniqueWorkName, existingWorkPolicy, workRequest);
        }

        /**
         * 开启并执行唯一性工作任务
         *
         * @param context
         * @param uniqueWorkName
         * @param existingWorkPolicy
         * @return
         */
        public Operation enqueueUniqueWork(@NonNull Context context,
                                           @NonNull String uniqueWorkName,
                                           @NonNull ExistingWorkPolicy existingWorkPolicy) {
            return WorkManager.getInstance(context)
                    .enqueueUniqueWork(uniqueWorkName, existingWorkPolicy, workRequest);
        }
    }

    public static final class PeriodicWork {
        private PeriodicWorkRequest workRequest;

        private PeriodicWork(PeriodicWorkRequest workRequest) {
            this.workRequest = workRequest;
        }

        public PeriodicWorkRequest getWork() {
            return workRequest;
        }

        /**
         * 开启执行周期性任务
         *
         * @param context
         * @return
         */
        public Operation enqueue(Context context) {
            return WorkManager.getInstance(context).enqueue(workRequest);
        }

        /**
         * 开启并执行唯一性周期工作任务
         *
         * @param context
         * @param uniqueWorkName
         * @param existingPeriodicWorkPolicy
         * @return
         */
        public Operation enqueueUnique(
                @NonNull Context context,
                @NonNull String uniqueWorkName,
                @NonNull ExistingPeriodicWorkPolicy existingPeriodicWorkPolicy) {
            return WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(uniqueWorkName, existingPeriodicWorkPolicy, workRequest);
        }
    }

}
