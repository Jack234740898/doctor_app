package com.sftelehealth.doctor.app.view.fragment;


import androidx.lifecycle.Observer;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.app.listener.PreviewPrescriptionEventListener;
import com.sftelehealth.doctor.app.view.Navigator;
import com.sftelehealth.doctor.app.view.activity.BaseAppCompatActivity;
import com.sftelehealth.doctor.app.view.activity.PrescriptionActivity;
import com.sftelehealth.doctor.app.view.helper.SnackbarHelper;
import com.sftelehealth.doctor.app.view.utils.ActivityUtils;
import com.sftelehealth.doctor.app.view.viewmodel.PrescriptionActivityFragmentViewModel;
import com.sftelehealth.doctor.databinding.FragmentPreviewPrescriptionBinding;
import com.sftelehealth.doctor.databinding.MedicineItemLayoutBinding;
import com.sftelehealth.doctor.domain.model.Medicine;
import com.sftelehealth.doctor.domain.model.Prescription;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.sftelehealth.doctor.app.utils.Constant.CONSULT_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewPrescriptionFragment extends Fragment implements PreviewPrescriptionEventListener {

    private PrescriptionActivityFragmentViewModel viewModel;
    private FragmentPreviewPrescriptionBinding binding;

    private String prescription;

    public PreviewPrescriptionFragment() {
        // Required empty public constructor
    }

    public static PreviewPrescriptionFragment newInstance(String prescription) {

        Bundle args = new Bundle();

        if (prescription != null)
            args.putString("prescription", prescription);

        PreviewPrescriptionFragment fragment = new PreviewPrescriptionFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_prescription, container, false);

        viewModel = ((PrescriptionActivity) getActivity()).obtainViewModel(getActivity());
        if (getActivity().getIntent().getIntExtra(CONSULT_ID, 0) != 0)
            viewModel.prescription.get().setConsultId(getActivity().getIntent().getIntExtra(CONSULT_ID, 0));

        // Get the prescription object passed from the Case Details Screen, this means that the prescription is editable
        if (getArguments().getString("prescription") != null) {
            prescription = getArguments().getString("prescription");

            // will show the editable options on Action Bar and disable Send Medical Advice button
            viewModel.isPrescriptionEditable.set(true);
        } else {
            viewModel.isPrescriptionEditable.set(false);
        }

        ActivityUtils.setUpToolbarForBackPress((BaseAppCompatActivity) getActivity(), "Medical Advice");

        setUpObservers();

        setUpData();

        return binding.getRoot();
    }

    private void setUpData() {

        if (prescription != null) {
            // set the view model with this prescription
            Gson gson = new Gson();
            Prescription tempPrescription = gson.fromJson(prescription, Prescription.class);
            viewModel.prescription.set(tempPrescription);

            if(viewModel.prescription.get().getDietChart().size() > 0)
                viewModel.isPrescriptionEditable.set(false);
        }

        getActivity().invalidateOptionsMenu();

        binding.setPrescription(viewModel.prescription.get());
        binding.setViewmodel(viewModel);
        binding.setListener(this);
        prepareAddedMedicinesLayout();
        //binding.notifyPropertyChanged(BR.prescription);
    }

    private void setUpObservers() {
        viewModel.prescriptionCreated.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isPrescriptionCreated) {
                if(isPrescriptionCreated) {
                    SnackbarHelper.getCustomSnackBar(binding.getRoot(), "Medical Advice sent successfully!", null, SnackbarHelper.SnackbarTypes.INFO).show();
                    Observable.timer(3, TimeUnit.SECONDS, Schedulers.computation())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    Navigator.navigateToMainView(getContext());
                                }
                            });
                } else {
                    // prescription creation failed
                    SnackbarHelper.getCustomSnackBar(binding.getRoot(), "Unable to create Medical Advice! Please try again...", null, SnackbarHelper.SnackbarTypes.ERROR).show();
                    binding.sendPrescriptionButton.setClickable(true);
                }
//                SnackbarHelper.getCustomSnackBar(binding.getRoot(), "Medical Advice sent successfully!", null, SnackbarHelper.SnackbarTypes.INFO).show();
//                Observable.timer(3, TimeUnit.SECONDS, Schedulers.computation())
//                        .subscribe(new Consumer<Long>() {
//                            @Override
//                            public void accept(Long aLong) throws Exception {
//                                if(getActivity() != null)
//                                    Navigator.navigateToMainView(getActivity());
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//
//                            }
//                        });
            }
        });
    }

    private void prepareAddedMedicinesLayout() {
        binding.addedMedicinesContainer.removeAllViews();
        if (viewModel.prescription.get().getDietChart() != null && viewModel.prescription.get().getDietChart().size() > 0) {
            binding.dietChartContainer.setVisibility(View.VISIBLE);
        }

        // Appropriate message for diet chart
        if (viewModel.isPrescriptionEditable.get()) {
            // if diet chart has been already provided then show a different message
            if(viewModel.prescription.get().getDietChart().size() > 0)
                binding.dietChart.setText(R.string.diet_chart_provided_by_desktop_app);
            else
                binding.dietChart.setText(R.string.use_desktop_app_to_edit);
        } else
            binding.dietChart.setText(R.string.use_desktop_app_to_create_diet_chart_in_proper_format);

        // if its not a diet chart only then populate medicines
        if(!viewModel.prescription.get().isDietChart())
            for (Medicine tempMedicine : viewModel.prescription.get().getMedicine()) {

                MedicineItemLayoutBinding medicineItemBinding = DataBindingUtil.inflate(LayoutInflater.from(binding.getRoot().getContext()), R.layout.medicine_item_layout, binding.addedMedicinesContainer, false);

                if(tempMedicine.getDosage().length() == 5) {
                    String[] dosageArray = tempMedicine.getDosage().split("-");
                    tempMedicine.setDosageMorning(dosageArray[0]);
                    tempMedicine.setDosageAfternoon(dosageArray[1]);
                    tempMedicine.setDosageEvening(dosageArray[2]);
                }

                if (TextUtils.isEmpty(tempMedicine.getDosageMorning()) && TextUtils.isEmpty(tempMedicine.getDosageAfternoon()) && TextUtils.isEmpty(tempMedicine.getDosageEvening()) || tempMedicine.getDosage().equals("0-0-0")) {
                    // if all dosages are empty and duration is not then the graphic should not be visible
                    medicineItemBinding.dosageGraphic.setVisibility(View.GONE);
                    /*if(TextUtils.isEmpty(viewModel.medicine.get().getDuration())) {
                        // if duration is empty then do not show the dosage/duration
                        medicineItemBinding.dosage.setVisibility(View.INVISIBLE);
                    } else {
                        // if duration is set then show the dosage/duration
                        medicineItemBinding.dosage.setText(viewModel.medicine.get().getDuration() + " days");
                    }*/
                } else
                    medicineItemBinding.dosageGraphic.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(viewModel.medicine.get().getDuration())) // if duration is empty then do not show the dosage/duration
                    medicineItemBinding.dosage.setVisibility(View.GONE);
                else
                    medicineItemBinding.dosage.setVisibility(View.VISIBLE);

                // medicineItemBinding.dosageGraphic.setVisibility(tempMedicine.isDosageVisible()? View.VISIBLE : View.INVISIBLE);

                // for preview the removing graphic should not be present
                medicineItemBinding.removeMedicine.setVisibility(View.GONE);

                medicineItemBinding.setMedicine(tempMedicine);
                binding.addedMedicinesContainer.addView(medicineItemBinding.getRoot());
            }

        /*binding.addedMedicinesContainer.setMedicine(viewModel.medicine.get());
        binding.addedMedicinesContainer.notifyPropertyChanged(BR.medicine);*/
    }

    @Override
    public void createPrescription() {
        binding.sendPrescriptionButton.setClickable(false);
        viewModel.createPrescription();
    }

    /*@Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (viewModel.isPrescriptionEditable.get())
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_prescription:
                ((PrescriptionActivity) getActivity()).editPrescription();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}