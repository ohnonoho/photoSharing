package com.example.photosharing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProducerActivitySelectPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProducerActivitySelectPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProducerActivitySelectPhotosFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String TAG = "ProducerActivitySelectPhotosFragment";

    private View mView = null;

    private LinearLayout lnrImages;
    private Button btnAddPhotos;
    private Button btnShare;
    private ArrayList<String> imagesPathList;
    private Bitmap yourbitmap;
    private Bitmap resized;
    private final int PICK_IMAGE_MULTIPLE =1;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProducerActivitySelectPhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProducerActivitySelectPhotosFragment newInstance(String param1, String param2) {
        ProducerActivitySelectPhotosFragment fragment = new ProducerActivitySelectPhotosFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProducerActivitySelectPhotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_producer_activity_select_photos, container, false);
        mView = inflater.inflate(R.layout.fragment_producer_activity_select_photos, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lnrImages = (LinearLayout) mView.findViewById(R.id.lnrImages);
        btnAddPhotos = (Button) mView.findViewById(R.id.btnAddPhotos);
        btnShare = (Button) mView.findViewById(R.id.btnShare);
        //btnAddPhotos.setOnClickListener(this);
        btnAddPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CustomPhotoGalleryActivity.class);
                startActivityForResult(intent,PICK_IMAGE_MULTIPLE);
                Log.i(TAG, "btnAddPhotos pressed, intent send");

//            switch (mView.getId()) {
//                case R.id.btnAddPhotos:
//                    Intent intent = new Intent(getActivity(),CustomPhotoGalleryActivity.class);
//                    startActivityForResult(intent,PICK_IMAGE_MULTIPLE);
//                    Log.i(TAG, "btnAddPhotos pressed, intent send");
//                    break;
//                case R.id.btnShare:
//                    if(imagesPathList !=null){
//                        if(imagesPathList.size()>1) {
//                            Toast.makeText(getActivity(), imagesPathList.size() + " no of images are selected", Toast.LENGTH_SHORT).show();
//                        }else{
//                            Toast.makeText(getActivity(), imagesPathList.size() + " no of image are selected", Toast.LENGTH_SHORT).show();
//                        }
//                    }else{
//                        Toast.makeText(getActivity()," no images are selected", Toast.LENGTH_SHORT).show();
//                    }
//                    Log.i(TAG, "btnShare pressed, intent send");
//                    break;
//                default:
//                    Log.i(TAG, "btnAddPhotos pressed, intent send");
//            }
            }
        });
        //btnShare.setOnClickListener(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == PICK_IMAGE_MULTIPLE){
                imagesPathList = new ArrayList<String>();
                String[] imagesPath = data.getStringExtra("data").split("\\|");
                try{
                    lnrImages.removeAllViews();
                }catch (Throwable e){
                    e.printStackTrace();
                }
                for (int i=0;i<imagesPath.length;i++){
                    imagesPathList.add(imagesPath[i]);
                    yourbitmap = BitmapFactory.decodeFile(imagesPath[i]);
                    resized=    Bitmap.createScaledBitmap(yourbitmap, 200,300, true);
                    ImageView imageView = new ImageView(getActivity());
                    imageView.setImageBitmap(resized);
                    imageView.setAdjustViewBounds(true);
                    lnrImages.addView(imageView);
                }
            }
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
